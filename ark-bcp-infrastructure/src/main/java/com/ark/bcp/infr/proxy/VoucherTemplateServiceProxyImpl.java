package com.ark.bcp.infr.proxy;

import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.domain.infrservice.VoucherTemplateService;
import com.missfresh.voucher.bg.center.dto.template.VoucherTemplateDTO;
import com.missfresh.voucher.bg.center.request.VoucherInfoQueryDto;
import com.missfresh.voucher.bg.center.service.IVoucherTemplateService;
import com.missfresh.voucher.center.bean.DTO.CpVoucherDTO;
import com.missfresh.voucher.center.service.IVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Slf4j
@Service("voucherTemplateService")
public class VoucherTemplateServiceProxyImpl implements VoucherTemplateService {
    @Resource
    private IVoucherTemplateService iVoucherTemplateBgService;

    @Resource
    private IVoucherService iVoucherService;

    private VoucherTemplateDTO getVoucher(Long voucherId) {
        VoucherTemplateDTO voucherTemplateDTO = null;
        try {
            VoucherInfoQueryDto queryDto = VoucherInfoQueryDto.builder()
                    .id(voucherId)
                    .platform("AS")
                    .build();
            Result<VoucherTemplateDTO> result = iVoucherTemplateBgService.queryByVoucherId(queryDto);
            log.info("query voucher, id:{}, result:{}", voucherId, result);
            if (null != result && result.isSuccess() && result.getData() != null) {
                voucherTemplateDTO = result.getData();
            }
        } catch (Exception e) {
            log.error("query voucher error:{}", e);
            throw e;
        }

        return voucherTemplateDTO;
    }

    private CpVoucherDTO getVoucherByCode(String voucherCode) {
        if (StringUtils.isNotBlank(voucherCode)) {
            Result<CpVoucherDTO> voucherResult = iVoucherService.getVoucherInfoByInnerCode(voucherCode);
            log.info("query voucherDto:{}, voucherCode:{}", voucherResult, voucherCode);
            if (voucherResult != null && voucherResult.isSuccess()) {
                return voucherResult.getData();
            } else {
                log.error("query voucher by code failed, voucherCode:{}, result:{}", voucherCode, voucherResult);
                return null;
            }
        } else {
            log.error("voucher code invalid, voucherCode:{}", voucherCode);
            return null;
        }
    }

    @Override
    public boolean isNewUserVoucherCode(String voucherCode) {
        CpVoucherDTO voucher = this.getVoucherByCode(voucherCode);
        if (null == voucher) {
            return false;
        } else {
            return 1 == voucher.getOnlyNewUser();
        }
    }

    @Override
    public boolean isNewUserVoucher(Long voucherId) {
        VoucherTemplateDTO voucher = this.getVoucher(voucherId);
        if (voucher == null) {
            return false;
        }
        return voucher.getUserLevelLimit() == 1;
    }
}
