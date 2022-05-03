package com.swinburne.keycloak.userstorage.wordpress.client.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL) 
public class AccessTokenResponse {
    private String code;
    private String message;

    private Data data;
    private int statusCode;
    private boolean success;

    public AccessTokenResponse() {
        super();

    }

    public AccessTokenResponse(String code, String message, Data data, int statusCode, boolean success) {
		this.code = code;
		this.message = message;
		this.data = data;
		this.statusCode = statusCode;
		this.success = success;
	}

	@Override
    public String toString() {
        return "AccessTokenResponse [code=" + code + ", data=" + data + ", message=" + message + ", statusCode="
                + statusCode + ", success=" + success + "]";
    }

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

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL) 
    static public class Data {
        private String displayName;
        private String email;
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        private String firstName;
        private int id;
        private String lastName;
        private String nicename;
        private String token;
        
        public Data(){
            super();
        }

        public Data(String displayName, String email, String firstName, int id, String lastName, String nicename,
                String token) {
            this.displayName = displayName;
            this.email = email;
            this.firstName = firstName;
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
            return firstName;
        }
        public void setFirstname(String firstName) {
            this.firstName = firstName;
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
