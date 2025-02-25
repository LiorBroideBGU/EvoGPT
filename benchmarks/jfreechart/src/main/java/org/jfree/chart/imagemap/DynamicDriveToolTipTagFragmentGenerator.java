

package org.jfree.chart.imagemap;


public class DynamicDriveToolTipTagFragmentGenerator
        implements ToolTipTagFragmentGenerator {

    
    protected String title = "";

    
    protected int style = 1;

    
    public DynamicDriveToolTipTagFragmentGenerator() {
        super();
    }

    
    public DynamicDriveToolTipTagFragmentGenerator(String title, int style) {
        this.title = title;
        this.style = style;
    }

    
    @Override
    public String generateToolTipFragment(String toolTipText) {
        return " onMouseOver=\"return stm(['"
            + ImageMapUtils.javascriptEscape(this.title) + "','"
            + ImageMapUtils.javascriptEscape(toolTipText) + "'],Style["
            + this.style + "]);\"" + " onMouseOut=\"return htm();\"";
    }

}
