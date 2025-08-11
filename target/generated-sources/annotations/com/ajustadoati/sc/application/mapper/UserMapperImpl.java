package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.adapter.rest.dto.response.DefaultPaymentDto;
import com.ajustadoati.sc.adapter.rest.dto.response.SavingDto;
import com.ajustadoati.sc.adapter.rest.dto.response.UserDto;
import com.ajustadoati.sc.domain.DefaultPayment;
import com.ajustadoati.sc.domain.Saving;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAssociate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-11T17:42:00+0200",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.6 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Autowired
    private UserAssociateMapper userAssociateMapper;
    @Autowired
    private DefaultPaymentMapper defaultPaymentMapper;
    @Autowired
    private SavingMapper savingMapper;

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.id( user.getUserId() );
        userDto.totalSavings( calculateTotalSavings( user.getSavings() ) );
        userDto.firstName( user.getFirstName() );
        userDto.lastName( user.getLastName() );
        userDto.numberId( user.getNumberId() );
        userDto.mobileNumber( user.getMobileNumber() );
        userDto.email( user.getEmail() );
        userDto.company( user.getCompany() );
        userDto.associates( userAssociateListToAssociateDtoList( user.getAssociates() ) );
        userDto.defaultPayments( defaultPaymentListToDefaultPaymentDtoList( user.getDefaultPayments() ) );
        userDto.savings( savingListToSavingDtoList( user.getSavings() ) );

        return userDto.build();
    }

    protected List<AssociateDto> userAssociateListToAssociateDtoList(List<UserAssociate> list) {
        if ( list == null ) {
            return null;
        }

        List<AssociateDto> list1 = new ArrayList<AssociateDto>( list.size() );
        for ( UserAssociate userAssociate : list ) {
            list1.add( userAssociateMapper.toDto( userAssociate ) );
        }

        return list1;
    }

    protected List<DefaultPaymentDto> defaultPaymentListToDefaultPaymentDtoList(List<DefaultPayment> list) {
        if ( list == null ) {
            return null;
        }

        List<DefaultPaymentDto> list1 = new ArrayList<DefaultPaymentDto>( list.size() );
        for ( DefaultPayment defaultPayment : list ) {
            list1.add( defaultPaymentMapper.toDto( defaultPayment ) );
        }

        return list1;
    }

    protected List<SavingDto> savingListToSavingDtoList(List<Saving> list) {
        if ( list == null ) {
            return null;
        }

        List<SavingDto> list1 = new ArrayList<SavingDto>( list.size() );
        for ( Saving saving : list ) {
            list1.add( savingMapper.toDto( saving ) );
        }

        return list1;
    }
}
