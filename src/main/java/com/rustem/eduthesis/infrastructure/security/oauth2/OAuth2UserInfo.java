package com.rustem.eduthesis.infrastructure.security.oauth2;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getEmail();

    public abstract String getFirstName();

    public abstract String getLastName();

    public abstract String getAttributeKey();
}

class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getFirstName() {
        String name = (String) attributes.get("given_name");
        return name != null ? name : "";
    }

    @Override
    public String getLastName() {
        String name = (String) attributes.get("family_name");
        return name != null ? name : "";
    }

    @Override
    public String getAttributeKey() {
        return "email";
    }
}
