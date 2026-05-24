package com.example.speech.model;

import com.example.speech.util.CryptoConverter;
import com.example.speech.util.ImageConverter;
import jakarta.persistence.*;
import javafx.scene.image.Image;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "email_user", columnDefinition = "bytea")
    private String emailUser;

    @Column(name = "visible_name_user")
    private String visibleNameUser;
    @Column(name = "name_user")
    private String nameUser;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "password_user", columnDefinition = "bytea")
    private String passwordUser;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "birthday_user", columnDefinition = "bytea")
    private String birthdayUser;

    @Column(name = "user_photo")
    private byte[] photoUser;
    @Column(name = "user_status")
    private String statusUser;

    @Column(name = "plain_password")
    private String plainPassword;
    @Column(name = "unique_identity_computer")
    private String uniqueIdentityComputer;

    @Transient
    private Image photoImage;


    public Image getPhotoImage() {
        if (photoImage == null && photoUser != null) {
            try {
                photoImage = ImageConverter.convertBytesToImage(photoUser);
            } catch (Exception e) {
                System.err.println( e.getMessage());
                photoImage = ImageConverter.getDefaultImage();
            }
        }
        return photoImage != null ? photoImage : ImageConverter.getDefaultImage();
    }

    public User() { }

    public User(Integer idUser, String emailUser, String visibleNameUser, String nameUser, String passwordUser, String birthdayUser, byte[] photoUser, String statusUser, String plainPassword, String uniqueIdentityComputer) {
        this.idUser = idUser;
        this.emailUser = emailUser;
        this.visibleNameUser = visibleNameUser;
        this.nameUser = nameUser;
        this.passwordUser = passwordUser;
        this.birthdayUser = birthdayUser;
        this.photoUser = photoUser;
        this.statusUser = statusUser;
        this.plainPassword = plainPassword;
        this.uniqueIdentityComputer = uniqueIdentityComputer;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getVisibleNameUser() {
        return visibleNameUser;
    }

    public void setVisibleNameUser(String visibleNameUser) {
        this.visibleNameUser = visibleNameUser;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getPasswordUser() {
        return passwordUser;
    }

    public void setPasswordUser(String passwordUser) {
        this.passwordUser = passwordUser;
    }

    public String getBirthdayUser() {
        return birthdayUser;
    }

    public void setBirthdayUser(String birthdayUser) {
        this.birthdayUser = birthdayUser;
    }

    public byte[] getPhotoUser() {
        return photoUser;
    }

    public void setPhotoUser(byte[] photoUser) {
        this.photoUser = photoUser;
        this.photoImage = null;
    }

    public String getStatusUser() {
        return statusUser;
    }

    public void setStatusUser(String statusUser) {
        this.statusUser = statusUser;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getUniqueIdentityComputer() {
        return uniqueIdentityComputer;
    }

    public void setUniqueIdentityComputer(String uniqueIdentityComputer) {
        this.uniqueIdentityComputer = uniqueIdentityComputer;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return Objects.equals(idUser, user.idUser);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idUser);
    }
}
