package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;

/**
 * <p>
 * 银行账户信息表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
public interface IBankAccountService extends IService<BankAccount> {

    /**
     * 新增或更新银行账户
     *
     * @param bankAccountUpsertReqDTO 银行账户新增或更新请求体
     */
    void upsert(BankAccountUpsertReqDTO bankAccountUpsertReqDTO);

    /**
     * 查询当前用户银行账户信息
     *
     * @return 银行账户信息
     */
    BankAccountResDTO currentUserBankAccount();
}
