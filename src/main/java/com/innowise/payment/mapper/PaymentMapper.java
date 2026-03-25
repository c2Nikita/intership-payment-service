package com.innowise.payment.mapper;

import com.innowise.payment.dto.PaymentRequestDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Payment toEntity(PaymentRequestDto paymentRequestDto);

    PaymentResponseDto toDto(Payment payment);

    List<PaymentResponseDto> toDtoList(List<Payment> entities);

}
