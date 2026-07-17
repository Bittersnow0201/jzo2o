package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.customer.mapper.BankAccountMapper;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.IBankAccountService;
import com.jzo2o.common.utils.UserContext;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 银行账户信息表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
@Service
public class BankAccountServiceImpl
        extends ServiceImpl<BankAccountMapper, BankAccount>
        implements IBankAccountService {

    @Override
    public void upsert(BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        BankAccount bankAccount = BeanUtil.toBean(bankAccountUpsertReqDTO, BankAccount.class);
        // 以当前登录用户id作为主键，一个服务人员/机构对应一个银行账户
        bankAccount.setId(UserContext.currentUserId());
        saveOrUpdate(bankAccount);
    }

    @Override
    public BankAccountResDTO currentUserBankAccount() {
        BankAccount bankAccount = getById(UserContext.currentUserId());
        return BeanUtil.toBean(bankAccount, BankAccountResDTO.class);
    }
}
