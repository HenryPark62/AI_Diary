package backend.auth.dto;

import backend.auth.entity.TemporaryUser;
import lombok.Data;

@Data
public class SocialStartRes {
    private String state;
    private String provider;
    private String providerId;
    private String email;
    private String name;

    public static SocialStartRes from(TemporaryUser tu) {
        SocialStartRes res = new SocialStartRes();
        res.state = tu.getState();
        res.provider = tu.getProvider();
        res.providerId = tu.getProviderId();
        res.email = tu.getEmail();
        res.name = tu.getName();
        return res;
    }
}
