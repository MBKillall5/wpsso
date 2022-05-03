package com.swinburne.keycloak.userstorage.wordpress.client.pojo;

public class AccessTokenResponse {
    private String code;
    private String message;
    private Data data;
    private int statusCode;
    private boolean success;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    static public class Data {
        private String displayName;
        private String email;
        private String firstname;
        private int id;
        private String lastName;
        private String nicename;
        private String token;
        
        public Data(String displayName, String email, String firstname, int id, String lastName, String nicename,
                String token) {
            this.displayName = displayName;
            this.email = email;
            this.firstname = firstname;
            this.id = id;
            this.lastName = lastName;
            this.nicename = nicename;
            this.token = token;
        }
        public String getDisplayName() {
            return displayName;
        }
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getFirstname() {
            return firstname;
        }
        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public String getNicename() {
            return nicename;
        }
        public void setNicename(String nicename) {
            this.nicename = nicename;
        }
        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }

    }
}
