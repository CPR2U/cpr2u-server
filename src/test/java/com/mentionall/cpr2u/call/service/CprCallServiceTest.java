package com.mentionall.cpr2u.call.service;

import com.mentionall.cpr2u.call.domain.CprCall;
import com.mentionall.cpr2u.call.domain.CprCallStatus;
import com.mentionall.cpr2u.call.domain.Dispatch;
import com.mentionall.cpr2u.call.domain.DispatchStatus;
import com.mentionall.cpr2u.call.dto.CprCallNearUserDto;
import com.mentionall.cpr2u.call.dto.CprCallOccurDto;
import com.mentionall.cpr2u.call.dto.DispatchRequestDto;
import com.mentionall.cpr2u.call.dto.DispatchResponseDto;
import com.mentionall.cpr2u.call.repository.CprCallRepository;
import com.mentionall.cpr2u.call.repository.DispatchRepository;
import com.mentionall.cpr2u.user.domain.Address;
import com.mentionall.cpr2u.user.domain.AngelStatusEnum;
import com.mentionall.cpr2u.user.domain.User;
import com.mentionall.cpr2u.user.dto.UserSignUpDto;
import com.mentionall.cpr2u.user.repository.AddressRepository;
import com.mentionall.cpr2u.user.repository.UserRepository;
import com.mentionall.cpr2u.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CprCallServiceTest {

    @Autowired
    private CprCallService cprCallService;
    @Autowired
    private CprCallRepository cprCallRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private DispatchRepository dispatchRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Test
    @Transactional
    void getNowCallStatusNearUser() {
        //given
        User cprAngelUser = getUser(1);
        cprAngelUser.setAddress(addressRepository.findById(1L).orElse(null));
        cprAngelUser.setUserAngelStatus(AngelStatusEnum.ACQUIRED);
        userRepository.save(cprAngelUser);

        User cprAngelUserButNoPatient = getUser(2);
        cprAngelUserButNoPatient.setAddress(addressRepository.findById(2L).orElse(null));
        cprAngelUserButNoPatient.setUserAngelStatus(AngelStatusEnum.ACQUIRED);
        userRepository.save(cprAngelUserButNoPatient);

        User yetAngelUser = getUser(3);
        yetAngelUser.setAddress(addressRepository.findById(1L).orElse(null));
        userRepository.save(yetAngelUser);

        User caller = getUser(4);

        CprCall cprCall1 = new CprCall(caller, cprAngelUser.getAddress(), LocalDateTime.now(), new CprCallOccurDto("fullAddress", 37.56559872345163, 126.9779734762639));
        CprCall cprCall2 = new CprCall(caller, cprAngelUser.getAddress(), LocalDateTime.now(), new CprCallOccurDto("fullAddress", 37.56520212814079, 126.9771473198163));
        CprCall cprCall3 = new CprCall(caller, cprAngelUser.getAddress(), LocalDateTime.now(), new CprCallOccurDto("fullAddress", 37.56549899694667, 126.97488345790383));
        CprCall cprCall4 = new CprCall(caller, cprAngelUser.getAddress(), LocalDateTime.now(), new CprCallOccurDto("fullAddress", 37.56520212814079, 126.9771473198163));

        cprCallRepository.save(cprCall1);
        cprCallRepository.save(cprCall2);
        cprCallRepository.save(cprCall3);
        cprCallRepository.save(cprCall4);

        cprCallService.endCall(cprCall3.getId());

        //when
        CprCallNearUserDto callNearUserDtoForUser1 = cprCallService.getCallNearUser(cprAngelUser.getId());
        CprCallNearUserDto callNearUserDtoForUser2 = cprCallService.getCallNearUser(cprAngelUserButNoPatient.getId());
        CprCallNearUserDto callNearUserDtoForUser3 = cprCallService.getCallNearUser(yetAngelUser.getId());

        //then
        assertThat(callNearUserDtoForUser1.getCprCallDtoList().size()).isEqualTo(3);
        assertThat(callNearUserDtoForUser2.getCprCallDtoList().size()).isEqualTo(0);
        assertThat(callNearUserDtoForUser3.getCprCallDtoList().size()).isEqualTo(0);

    }

    @Test
    @Transactional
    void makeCall() {
        //given
        User user = getUser(1);

        //when
        Long callId = cprCallService.makeCall(new CprCallOccurDto("fullAddress", 37.56559872345163, 126.9779734762639), user.getId()).getCallId();

        //then
        CprCall cprCall = cprCallRepository.findById(callId).orElse(null);
        assertThat(cprCall.getStatus()).isEqualTo(CprCallStatus.IN_PROGRESS);

    }

    @Test
    @Transactional
    void endCall() {
        //given
        User caller = getUser(1);
        User dispatcher = getUser(2);

        //when
        Long callId = cprCallService.makeCall(new CprCallOccurDto("fullAddress", 37.56559872345163, 126.9779734762639), caller.getId()).getCallId();
        DispatchResponseDto dispatchInfo = dispatchService.dispatch(dispatcher.getId(), new DispatchRequestDto(callId));
        cprCallService.endCall(callId);

        //then
        CprCall cprCall = cprCallRepository.findById(callId).orElse(null);
        assertThat(cprCall.getStatus()).isEqualTo(CprCallStatus.END_SITUATION);
        Dispatch dispatch = dispatchRepository.findById(dispatchInfo.getDispatchId()).orElse(null);
        assertThat(dispatch.getStatus()).isEqualTo(DispatchStatus.END_SITUATION);

    }

    public User getUser(int number) {
        UserSignUpDto userSignUpDto = new UserSignUpDto("nickname" + number, "phoneNumber" + number, "deviceToken");
        userService.signup(userSignUpDto);
        return userRepository.findByPhoneNumber("phoneNumber" + number).orElse(null);
    }
}