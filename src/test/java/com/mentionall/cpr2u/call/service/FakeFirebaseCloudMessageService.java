package com.mentionall.cpr2u.call.service;

import java.util.List;
import java.util.Map;

public class FakeFirebaseCloudMessageService extends FirebaseCloudMessageService {

    @Override
    public void sendFcmMessage(List<String> deviceTokenToSendList, String title, String body, Map<String, String> data) {
    }
}
