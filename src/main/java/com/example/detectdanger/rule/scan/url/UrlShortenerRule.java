package com.example.detectdanger.rule.scan.url;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@Component
public class UrlShortenerRule implements DetectionRule {
    private static final int WEIGHT = 15;

    private static final Set<String> SHORTENER_DOMAINS = Set.of(
            "bit.ly",
            "tinyurl.com",
            "t.co",
            "ow.ly",
            "is.gd",
            "buff.ly",
            "cutt.ly",
            "shorturl.at"
    );
    @Override
    public String getCode() {
        return "URL_SHORTENER";
    }

    @Override
    public String getName() {
        return "URL Shortener Detection";
    }

    @Override
    public int getWeight() {
        return WEIGHT;
    }


    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.URL;
    }

    @Override
    public RuleStatus getStatus(){
        return RuleStatus.ACTIVE;
    }

    @Override
    public String getVersion(){
        return "1.1";
    }

    @Override
    public RuleResult evaluate(String input,InputType inputType){
        if(supports(inputType)){
            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    getName(),
                    List.of(getName())
            );
        }
        return new RuleResult(
                getCode(),
                true,
                0,
                null,
                List.of()
        );




    }





}
