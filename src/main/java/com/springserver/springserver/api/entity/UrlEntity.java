package com.springserver.springserver.api.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "url_table",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "shorturl_unique",
                        columnNames = "short_url"
                )
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UrlEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "original_url", nullable = false)
    private String origUrl;

    @Column(name = "short_url", nullable = false)
    private String shortUrl;

    @Column(name = "created_time", nullable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @CreationTimestamp
    private LocalDateTime dateCreated;
}
