package com.example.detectdanger.service.moderator;

import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporterReputationService {
    private final UserRepository userRepository;

    public void handleVerifyReport(User user){
        int current = user.getReputationScore();
        int newScore = Math.min(100,current + 5);
        user.setReputationScore(newScore);
        user.setVerifiedReports(user.getVerifiedReports() +1);

        userRepository.save(user);

    }
    public void handleRejected(User user) {
        int current = user.getReputationScore();
        int newScore = Math.max(current - 5, 0);
        user.setReputationScore(newScore);
        user.setRejectedReports(user.getRejectedReports() + 1);

        userRepository.save(user);
    }


}
