package com.example.detectdanger.entity.Enum;

public enum RuleType {
    SET_LOOKUP,       // Kiểm tra giá trị có nằm trong danh sách đen không (VD: domain rác)
    REGEX,            // Kiểm tra khớp biểu thức chính quy (Regex)
    PREFIX_MATCH,     // Kiểm tra đầu số/tiền tố (VD: đầu số quốc tế lừa đảo)
    KEYWORD_CONTAINS  // Kiểm tra có chứa từ khóa nguy hiểm không
}
