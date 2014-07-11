package ru.aklimov.wsdlcomparator.domain.descriptors;

/**
 * Created with IntelliJ IDEA.
 * User: aklimov
 * Date: 20.05.13
 * Time: 19:07
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationDescriptor {
    static public enum ANNOTATION_TYPE{APPINFO,DOCUMENTATION};

    private ANNOTATION_TYPE annotationType;
    private String value;

    public ANNOTATION_TYPE getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(ANNOTATION_TYPE annotationType) {
        this.annotationType = annotationType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
