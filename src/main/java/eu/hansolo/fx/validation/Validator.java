///*
// * Copyright (c) 2013. Gerrit Grunwald
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package eu.hansolo.fx.validation;
//
//import javafx.beans.property.DoubleProperty;
//import javafx.beans.property.SimpleDoubleProperty;
//import javafx.geometry.Pos;
//
//
///**
// * Created by
// * User: hansolo
// * Date: 10.04.13
// * Time: 09:25
// */
//public class Validator {
//    public static enum State {
//        VALID,
//        INVALID,
//        INFO,
//        OPTIONAL,
//        CLEAR
//    }
//    private DoubleProperty alpha;
//    private boolean        valid;
//    private State          state;
//    private Pos            validatorPosition;
//
//
//    // ******************** Constructors **********************************
//    public Validator(final State STATE) {
//        this(STATE, Pos.TOP_LEFT);
//    }
//    public Validator(final State STATE, final Pos POSITION) {
//        alpha             = new SimpleDoubleProperty(1.0);
//        state             = STATE;
//        validatorPosition = POSITION;
//    }
//
//
//    // ******************** Methods ***************************************
//    public State getState() {
//        return state;
//    }
//    public void setState(final State STATE) {
//        state = STATE;
//    }
//
//    public Pos getValidatorPosition() {
//        return validatorPosition;
//    }
//    public void setValidatorPosition(final Pos VALIDATOR_POSITION) {
//        validatorPosition = VALIDATOR_POSITION;
//    }
//
//    public double getAlpha() {
//        return alpha.get();
//    }
//    public void setAlpha(final double ALPHA) {
//        alpha.set(ALPHA);
//    }
//    public DoubleProperty alphaProperty() {
//        return alpha;
//    }
//}
