

package org.jfree.chart.event;

import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.internal.Args;


public class AnnotationChangeEvent extends ChartChangeEvent {

    
    private final Annotation annotation;

    
    public AnnotationChangeEvent(Object source, Annotation annotation) {
        super(source);
        Args.nullNotPermitted(annotation, "annotation");
        this.annotation = annotation;
    }

    
    public Annotation getAnnotation() {
        return this.annotation;
    }

}
