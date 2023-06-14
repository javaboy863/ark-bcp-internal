package com.ark.bcp.domain.infrservice;

/**
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public interface VoucherTemplateService {

    boolean isNewUserVoucher(Long voucherId);

    boolean isNewUserVoucherCode(String voucherCode);

}
