package com.mentionall.cpr2u.call.service;

import com.mentionall.cpr2u.call.domain.CprCall;
import com.mentionall.cpr2u.call.domain.Dispatch;
import com.mentionall.cpr2u.call.domain.DispatchStatus;
import com.mentionall.cpr2u.call.dto.*;
import com.mentionall.cpr2u.call.repository.CprCallRepository;
import com.mentionall.cpr2u.call.repository.DispatchRepository;
import com.mentionall.cpr2u.user.domain.Address;
import com.mentionall.cpr2u.user.domain.AngelStatusEnum;
import com.mentionall.cpr2u.user.domain.User;
import com.mentionall.cpr2u.user.repository.AddressRepository;
import com.mentionall.cpr2u.util.exception.CustomException;
import com.mentionall.cpr2u.util.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CprCallService {
    private final CprCallRepository cprCallRepository;
    private final DispatchRepository dispatchRepository;
    private final AddressRepository addressRepository;

    public CprCallNearUserDto getCallNearUser(User user) {
        AngelStatusEnum userAngelStatus = user.getStatus();
        if (userAngelStatus != AngelStatusEnum.ACQUIRED) {
            return new CprCallNearUserDto(
                    userAngelStatus,
                    false,
                    new ArrayList<>()
            );
        }
        if (user.getAddress() == null) {
            throw new CustomException(ResponseCode.BAD_REQUEST_ADDRESS_NOT_SET);
        }
        List<CprCallDto> cprCallDtoList = cprCallRepository.findAllCallInProcessByAddress(user.getAddress().getId());
        return new CprCallNearUserDto(
                userAngelStatus,
                cprCallDtoList.size() > 0,
                cprCallDtoList
        );
    }

    public CprCallIdDto makeCall(CprCallOccurDto cprCallOccurDto, User user) {
        Address callAddress = addressRepository.findByFullAddress(cprCallOccurDto.getFullAddress().split(" "))
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND_FAILED_TO_MATCH_ADDRESS));

        CprCall cprCall = new CprCall(user, callAddress, LocalDateTime.now(), cprCallOccurDto);
        cprCallRepository.save(cprCall);
        //TOD FCM 붙이기

        return new CprCallIdDto(cprCall.getId());
    }

    public void endCall(Long callId) {
        CprCall cprCall = cprCallRepository.findById(callId).orElseThrow(
                () -> new CustomException(ResponseCode.NOT_FOUND_CPRCALL)
        );
        //TODO FCM 붙이기
        cprCall.endSituationCprCall();
        cprCallRepository.save(cprCall);
        List<Dispatch> dispatchList = dispatchRepository.findAllByCprCallId(cprCall.getId());
        for (Dispatch dispatch : dispatchList) {
            dispatch.setStatus(DispatchStatus.END_SITUATION);
            dispatchRepository.save(dispatch);
        }

    }

    public CprCallGuideResponseDto getNumberOfAngelsDispatched(Long callId) {
        CprCall cprCall = cprCallRepository.findById(callId).orElseThrow(
                () -> new CustomException(ResponseCode.NOT_FOUND_CPRCALL)
        );

        List<Dispatch> dispatchList = dispatchRepository.findAllByCprCallId(callId);
        return new CprCallGuideResponseDto(dispatchList.size());
    }
}
