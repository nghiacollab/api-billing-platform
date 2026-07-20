package com.abp.api_billing_platform.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.Invoice;
import com.abp.api_billing_platform.entity.InvoiceItem;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.enums.BillingType;
import com.abp.api_billing_platform.interfaces.ApiUsageProjection;
import com.abp.api_billing_platform.repository.InvoiceItemRepository;
import com.abp.api_billing_platform.repository.InvoiceRepository;
import com.abp.api_billing_platform.repository.TransactionLogRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class BillingBatchJob {
  private final TransactionLogRepository transactionLogRepository;
  private final InvoiceRepository invoiceRepository;
  private final InvoiceItemRepository invoiceItemRepository;
  private final EntityManager entityManager;

  public BillingBatchJob(TransactionLogRepository transactionLogRepository,
      InvoiceRepository invoiceRepository,
      InvoiceItemRepository invoiceItemRepository,
      EntityManager entityManager) {
    this.transactionLogRepository = transactionLogRepository;
    this.invoiceRepository = invoiceRepository;
    this.invoiceItemRepository = invoiceItemRepository;
    this.entityManager = entityManager;
  }

  @Scheduled(cron = "0 0 0 1 * ?")
  @Transactional
  public void generateMonthlyInvoices() {
    System.out.println("🏁 Khởi chạy Cronjob tổng hợp hóa đơn dựa trên Schema hiện tại...");

    LocalDate lastMonth = LocalDate.now().minusMonths(1);
    LocalDateTime startTime = LocalDateTime.of(lastMonth.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()), LocalTime.MAX);

    String currentCycle = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

    List<ApiUsageProjection> usages = transactionLogRepository.aggregateApiUsageForPeriod(startTime, endTime);

    if (usages.isEmpty()) {
      System.out.println("ℹ️ Không có dữ liệu sử dụng API nào hợp lệ trong chu kỳ " + currentCycle);
      return;
    }

    Map<Long, List<ApiUsageProjection>> usageByConsumer = usages.stream()
        .collect(Collectors.groupingBy(ApiUsageProjection::getConsumerId));

    for (Map.Entry<Long, List<ApiUsageProjection>> entry : usageByConsumer.entrySet()) {
      Long consumerId = entry.getKey();
      List<ApiUsageProjection> apiUsages = entry.getValue();
      Consumer consumer = entityManager.getReference(Consumer.class, consumerId);

      // Khởi tạo Invoice tổng (Master)
      Invoice invoice = new Invoice();
      invoice.setConsumer(consumer); // Dùng Proxy
      invoice.setBillingCycle(currentCycle);
      if (consumer.getBillingType() == BillingType.PREPAID) {
        // PREPAID: Đã trừ tiền trực tiếp lúc gọi API -> Đánh dấu là PAID (Biên lai
        // quyết toán)
        invoice.setStatus("PAID");
      } else {
        // POSTPAID: Chưa trừ tiền -> Đánh dấu UNPAID để gửi thông báo đòi tiền
        invoice.setStatus("UNPAID");
      }

      BigDecimal totalInvoiceAmount = BigDecimal.ZERO;
      List<InvoiceItem> items = new ArrayList<>();

      // Duyệt danh sách các API đã sử dụng để sinh InvoiceItem (Detail)
      for (ApiUsageProjection usage : apiUsages) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice); // Gắn kết mối quan hệ
        item.setMerchantApi(entityManager.getReference(MerchantApi.class, usage.getMerchantApiId())); // Dùng Proxy
        item.setTotalQuantity(usage.getTotalQuantity());
        item.setTotalAmount(usage.getTotalAmount());

        items.add(item);
        totalInvoiceAmount = totalInvoiceAmount.add(usage.getTotalAmount());
      }

      // Gán tổng tiền sau khi đã sum tất cả các item
      invoice.setTotalAmount(totalInvoiceAmount);

      // Lưu Invoice xuống trước để sinh ID tự tăng trong MySQL
      Invoice savedInvoice = invoiceRepository.save(invoice);

      // Gán ngược lại ID vừa sinh cho các Item rồi lưu hàng loạt (Cascade thay thế)
      for (InvoiceItem item : items) {
        item.setInvoice(savedInvoice);
      }
      invoiceItemRepository.saveAll(items);
    }
    System.out.println("🎉 Đã hoàn thành xử lý và lưu toàn bộ Hóa đơn/Item cho chu kỳ: " + currentCycle);
  }
}
