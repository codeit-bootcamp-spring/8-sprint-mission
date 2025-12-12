package com.sprint.mission.discodeit.entity;

/*
    User
   - 유저 엔티티

    [필드 설명]
    • name        : 유저의 이름 (수정 O)
    • gender      : 유저의 성별 (수정 O)
    • age         : 유저의 나이 (수정 O)

    [메서드]
    • update(User user)    : User 객체의 값을 현재 객체에 반영 및 updateCall()로 updatedAt 수정.
 */

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class User extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // User 이름 (수정 O)
    private String name;

    // User 성별 (수정 O)
    private String gender;

    // User 나이 (수정 O)
    private Integer age;

    public User(String name, String gender, Integer age) {
        super();
        this.name = name;
        this.gender = gender;
        this.age = age;
    }

    public void update(User user) {
        this.name = user.name;
        this.gender = user.gender;
        this.age = user.age;
        updateCall();
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                '}';
    }
}
