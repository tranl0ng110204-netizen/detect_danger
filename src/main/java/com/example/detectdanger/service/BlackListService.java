package com.example.detectdanger.service;

import com.example.detectdanger.dto.blacklist.BlackListRequest;
import com.example.detectdanger.dto.blacklist.BlackListResponse;
import com.example.detectdanger.entity.BlackList;
import com.example.detectdanger.repository.BlackListRepository;
import com.example.detectdanger.entity.BlackListSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlackListService {
    private final BlackListRepository blackListRepository;
    private final NormalizeService normalizeService;

    public BlackListResponse createBlackList(BlackListRequest request){
        String normalizedValue = normalizeService.normalize(
                request.inputType(),
                request.value()
        );
        BlackList blacklist = BlackList.builder()
                .inputType(request.inputType())
                .normalizedValue(normalizedValue)
                .reason(request.reason())
                .source(BlackListSource.ADMIN)
                .active(true)
                .build();

        BlackList saved = blackListRepository.save(blacklist);
        return toResponse(blacklist);
    }

    private BlackListResponse toResponse(BlackList blacklist){
        return new BlackListResponse(
                blacklist.getId(),
                blacklist.getInputType(),
                blacklist.getNormalizedValue(),
                blacklist.getReason(),
                blacklist.getSource(),
                blacklist.isActive(),
                blacklist.getCreatedAt(),
                blacklist.getUpdatedAt()
        );

    }

}
