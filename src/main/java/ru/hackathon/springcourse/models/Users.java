    package ru.hackathon.springcourse.models;


    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotBlank;

    @Entity
    @Table(name="users")
    public class Users {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name="id")
        private int id;

        @NotBlank(message = "Email не может быть пустым")
        @Column(name="email")
        private String email;

        @NotBlank(message = "Пароль не может быть пустым")
        @Column(name="password")
        private String password;

        @NotBlank(message = "Повторите пароль")
        @Transient
        private String confirmPassword;

        @Column(name="role")
        private String role;

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
