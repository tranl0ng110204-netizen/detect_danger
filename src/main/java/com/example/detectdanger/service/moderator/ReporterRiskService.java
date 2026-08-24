package com.example.detectdanger.service.moderator;

import com.example.detectdanger.entity.Enum.ReporterStatus;
import com.example.detectdanger.entity.User;
import org.springframework.stereotype.Service;

@Service
public class ReporterRiskService {
    public ReporterStatus evaluateUser(User user){
        int reputation = user.getReputationScore();
        if(reputation<30){
            return ReporterStatus.RESTRICTED;
        }
        if(reputation<70){
            return ReporterStatus.SUSPICIOUS;
        }
        return ReporterStatus.NORMAL;
    }
}
