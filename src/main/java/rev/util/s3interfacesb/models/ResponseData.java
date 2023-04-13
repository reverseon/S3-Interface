package rev.util.s3interfacesb.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseData<T> {
    private boolean success;
    private String message;
    private T data;

}
