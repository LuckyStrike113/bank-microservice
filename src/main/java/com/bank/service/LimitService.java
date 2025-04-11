package com.bank.service;

import com.bank.dto.LimitRequest;
import com.bank.entity.Limit;
import com.bank.repository.LimitRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LimitService {

    private final LimitRepository limitRepository;

    public void setLimit(LimitRequest request) {
        Limit limit = new Limit();
        limit.setLimitSum(request.getLimitSum());
        limit.setLimitDatetime(OffsetDateTime.now());
        limit.setLimitCurrencyShortname("USD");
        limit.setExpenseCategory(request.getExpenseCategory());
        limitRepository.save(limit);
    }
}