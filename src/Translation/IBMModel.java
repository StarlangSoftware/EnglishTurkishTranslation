package Translation;

import java.util.ArrayList;

public abstract class IBMModel {

    public abstract ArrayList<PartialTranslation> translate(PartialTranslation current, int index);
    public abstract void loadModel(String modelFile);
}
