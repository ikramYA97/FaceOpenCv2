package com.example.demo1;

import com.jfoenix.controls.JFXRadioButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class face_ICAO_Criteria {
    private StringProperty description = new SimpleStringProperty();
    private ObjectProperty<JFXRadioButton> radioButton      = new SimpleObjectProperty<>();

    public face_ICAO_Criteria(String desc, JFXRadioButton radioBtn ) {
        if ( desc != null && !desc.isEmpty() )
            this.description.set( desc );
        this.radioButton.set( radioBtn );
    }

    public   StringProperty descriptionProperty() {
        return this.description;
    }

    public   String getDescription() {
        return descriptionProperty().get();
    }

    public   void setDescription(   String desc ) {
        descriptionProperty().set( desc );
    }

    public   ObjectProperty<JFXRadioButton> radioButtonProperty() {
        return this.radioButton;
    }

    public JFXRadioButton getRadioButton() {
        return radioButtonProperty().get();
    }

    public void setRadioButton(   JFXRadioButton value ) {
        radioButtonProperty().set(value);
    }

}
