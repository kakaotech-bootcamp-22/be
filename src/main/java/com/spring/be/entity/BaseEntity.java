package com.spring.be.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.spring.be.util.CustomLocalDateTimeSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class) // Custom serializer 적용
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)  // Default deserializer 적용
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class) // Custom serializer 적용
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)  // Default deserializer 적용
    private LocalDateTime updatedAt;

    @Column
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class) // Custom serializer 적용
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)  // Default deserializer 적용
    private LocalDateTime deletedAt;
}
