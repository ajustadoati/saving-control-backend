package com.ajustadoati.sc.application.mapper;

import com.ajustadoati.sc.adapter.rest.dto.response.AssociateDto;
import com.ajustadoati.sc.domain.User;
import com.ajustadoati.sc.domain.UserAssociate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-02T22:18:04+0200",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.5 (Amazon.com Inc.)"
)
@Component
public class UserAssociateMapperImpl implements UserAssociateMapper {

    @Override
    public AssociateDto toDto(UserAssociate user) {
        if ( user == null ) {
            return null;
        }

        AssociateDto.AssociateDtoBuilder associateDto = AssociateDto.builder();

        associateDto.id( userUserAssociateUserId( user ) );
        associateDto.firstName( userUserAssociateFirstName( user ) );
        associateDto.lastName( userUserAssociateLastName( user ) );
        associateDto.numberId( userUserAssociateNumberId( user ) );
        associateDto.relationship( user.getRelationship() );

        return associateDto.build();
    }

    private Integer userUserAssociateUserId(UserAssociate userAssociate) {
        User userAssociate1 = userAssociate.getUserAssociate();
        if ( userAssociate1 == null ) {
            return null;
        }
        return userAssociate1.getUserId();
    }

    private String userUserAssociateFirstName(UserAssociate userAssociate) {
        User userAssociate1 = userAssociate.getUserAssociate();
        if ( userAssociate1 == null ) {
            return null;
        }
        return userAssociate1.getFirstName();
    }

    private String userUserAssociateLastName(UserAssociate userAssociate) {
        User userAssociate1 = userAssociate.getUserAssociate();
        if ( userAssociate1 == null ) {
            return null;
        }
        return userAssociate1.getLastName();
    }

    private String userUserAssociateNumberId(UserAssociate userAssociate) {
        User userAssociate1 = userAssociate.getUserAssociate();
        if ( userAssociate1 == null ) {
            return null;
        }
        return userAssociate1.getNumberId();
    }
}
