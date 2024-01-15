package tech.obiteaaron.winter.common.tools.result;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -7408234921939812592L;

    T data;

    long total;
}
