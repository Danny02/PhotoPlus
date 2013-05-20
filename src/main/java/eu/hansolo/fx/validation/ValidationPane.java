/*
 * Copyright (c) 2013. Gerrit Grunwald
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package eu.hansolo.fx.validation;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Created by User: hansolo Date: 08.04.13 Time: 07:24
 */
public class ValidationPane extends Region {

    private static final int IMG_SIZE = 12;
    private static final int OFFSET = 6;
    private static final Image IMG_VALID = new Image(ValidationPane.class.getResource("valid.png").toExternalForm(), IMG_SIZE, IMG_SIZE, true, false);
    private static final Image IMG_INVALID = new Image(ValidationPane.class.getResource("invalid.png").toExternalForm(), IMG_SIZE, IMG_SIZE, true, false);
    private static final Image IMG_INFO = new Image(ValidationPane.class.getResource("info.png").toExternalForm(), IMG_SIZE, IMG_SIZE, true, false);
    private static final Image IMG_OPTIONAL = new Image(ValidationPane.class.getResource("optional.png").toExternalForm(), IMG_SIZE, IMG_SIZE, true, false);
    private Canvas canvas;
    private GraphicsContext ctx;
    private ObservableMap<Node, Validator> validatorMap;
    private ObservableMap<Node, Timeline> faderMap;
    private InvalidationListener visibilityListener;

    // ******************** Constructors **************************************
    public ValidationPane() {
        init();
        initGraphics();
        registerListeners();
    }

    private void init() {
        validatorMap = FXCollections.observableHashMap();
        faderMap = FXCollections.observableHashMap();
        visibilityListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                draw();
            }
        };
    }

    private void initGraphics() {
        setMouseTransparent(true);
        canvas = new Canvas(getPrefWidth(), getPrefHeight());
        ctx = canvas.getGraphicsContext2D();
        DropShadow shadow = new DropShadow();
        shadow.setRadius(3);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setBlurType(BlurType.TWO_PASS_BOX);
        shadow.setOffsetY(1);
        canvas.setEffect(shadow);

        getChildren().setAll(canvas);
    }

    private void registerListeners() {
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {

                canvas.setWidth(getWidth());
                ctx.clearRect(0, 0, getWidth(), getHeight());
                draw();
            }
        });
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                canvas.setHeight(getHeight());
                ctx.clearRect(0, 0, getWidth(), getHeight());
                draw();
            }
        });
        validatorMap.addListener(new MapChangeListener<Node, Validator>() {
            @Override
            public void onChanged(Change<? extends Node, ? extends Validator> change) {
                draw();
            }
        });
    }

    // ******************** Methods *******************************************
    public void add(final Node NODE) {
        add(Pos.TOP_LEFT, NODE);
    }

    public void add(final Pos POSITION, final Node NODE) {
        if (validatorMap.keySet().contains(NODE)) {
            return;
        }
        validatorMap.put(NODE, new Validator(Validator.State.CLEAR, POSITION));
        NODE.visibleProperty().addListener(visibilityListener);
    }

    public void addAll(final Node... NODES) {
        addAll(Pos.TOP_LEFT, NODES);
    }

    public void addAll(final Pos POS, final Node... NODES) {
        for (Node node : NODES) {
            add(POS, node);
        }
    }

    public void remove(final Node NODE) {
        if (validatorMap.containsKey(NODE)) {
            validatorMap.remove(NODE);
        }
    }

    public void clear() {
        validatorMap.clear();
    }

    public Validator.State getState(final Node NODE) {
        return (validatorMap.keySet().contains(NODE)) ? validatorMap.get(NODE).getState() : Validator.State.CLEAR;
    }

    public void setState(final Node NODE, final Validator.State STATE) {
        if (validatorMap.keySet().contains(NODE)) {
            validatorMap.get(NODE).setState(STATE);
            validatorMap.get(NODE).setAlpha(1.0);
            draw();
            if (Validator.State.VALID == STATE) {
                fireValidationEvent(new ValidationEvent(NODE, this, null, ValidationEvent.VALID));
                fadeOut(NODE);
            } else if (Validator.State.INVALID == STATE) {
                fireValidationEvent(new ValidationEvent(NODE, this, null, ValidationEvent.INVALID));
                stopFadingIfNeeded(NODE);
            } else if (Validator.State.INFO == STATE) {
                fireValidationEvent(new ValidationEvent(NODE, this, null, ValidationEvent.INFO));
                stopFadingIfNeeded(NODE);
            } else if (Validator.State.OPTIONAL == STATE) {
                fireValidationEvent(new ValidationEvent(NODE, this, null, ValidationEvent.OPTIONAL));
                stopFadingIfNeeded(NODE);
            } else {
                fireValidationEvent(new ValidationEvent(NODE, this, null, ValidationEvent.CLEAR));
                stopFadingIfNeeded(NODE);
            }
        }
    }

    private void fadeOut(final Node NODE) {
        validatorMap.get(NODE).alphaProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                draw();
            }
        });

        KeyValue keyValueVisible = new KeyValue(validatorMap.get(NODE).alphaProperty(), 1);
        KeyValue keyValueInvisible = new KeyValue(validatorMap.get(NODE).alphaProperty(), 0, Interpolator.EASE_IN);

        KeyFrame kf1 = new KeyFrame(Duration.millis(0), keyValueVisible);
        KeyFrame kf2 = new KeyFrame(Duration.millis(1000), keyValueVisible);
        KeyFrame kf3 = new KeyFrame(Duration.millis(1500), keyValueInvisible);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().setAll(kf1, kf2, kf3);
        faderMap.put(NODE, timeline);
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                faderMap.remove(NODE);
            }
        });
        timeline.play();
    }

    private void stopFadingIfNeeded(final Node NODE) {
        if (faderMap.keySet().contains(NODE)) {
            faderMap.get(NODE).stop();
            faderMap.remove(NODE);
        }
    }

    private void draw() {
        if (validatorMap.isEmpty()) {
            return;
        }

        double[] indicatorPos = new double[2];
        for (Node node : validatorMap.keySet()) {
            Validator validator = validatorMap.get(node);

            ctx.save();
            ctx.setGlobalAlpha(validator.getAlpha());
            Point2D nodeMinPos = node.localToScene(node.getLayoutBounds().getMinX(), node.getLayoutBounds().getMinY());
            Point2D nodeMaxPos = node.localToScene(node.getLayoutBounds().getMaxX(), node.getLayoutBounds().getMaxY());

            if (nodeMinPos.getX() > 0 && nodeMinPos.getY() > 0) {
                if (Pos.CENTER_LEFT == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMinPos.getX() - OFFSET;
                    indicatorPos[1] = nodeMinPos.getY() + (node.getLayoutBounds().getHeight() - IMG_SIZE) * 0.5;
                } else if (Pos.BOTTOM_LEFT == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMinPos.getX() - OFFSET;
                    indicatorPos[1] = nodeMaxPos.getY() - OFFSET;
                } else if (Pos.TOP_RIGHT == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMaxPos.getX() - OFFSET;
                    indicatorPos[1] = nodeMinPos.getY() - OFFSET;
                } else if (Pos.CENTER_RIGHT == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMaxPos.getX() - OFFSET;
                    indicatorPos[1] = nodeMinPos.getY() + (node.getLayoutBounds().getHeight() - IMG_SIZE) * 0.5;
                } else if (Pos.BOTTOM_RIGHT == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMaxPos.getX() - OFFSET;
                    indicatorPos[1] = nodeMaxPos.getY() - OFFSET;
                } else if (Pos.TOP_CENTER == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMinPos.getX() + (node.getLayoutBounds().getWidth()) * 0.5 - OFFSET;
                    indicatorPos[1] = nodeMinPos.getY() - OFFSET;
                } else if (Pos.BOTTOM_CENTER == validator.getValidatorPosition()) {
                    indicatorPos[0] = nodeMinPos.getX() + (node.getLayoutBounds().getWidth()) * 0.5 - OFFSET;
                    indicatorPos[1] = nodeMaxPos.getY() - OFFSET;
                } else {
                    indicatorPos[0] = nodeMinPos.getX() - OFFSET;
                    indicatorPos[1] = nodeMinPos.getY() - OFFSET;
                }

                Validator.State state = validatorMap.get(node).getState();
                ctx.clearRect(indicatorPos[0], indicatorPos[1], 12, 12);

                if (node.isVisible()) {
                    if (Validator.State.VALID == state) {
                        ctx.drawImage(IMG_VALID, indicatorPos[0], indicatorPos[1]);
                    } else if (Validator.State.INVALID == state) {
                        ctx.drawImage(IMG_INVALID, indicatorPos[0], indicatorPos[1]);
                    } else if (Validator.State.INFO == state) {
                        ctx.drawImage(IMG_INFO, indicatorPos[0], indicatorPos[1]);
                    } else if (Validator.State.OPTIONAL == state) {
                        ctx.drawImage(IMG_OPTIONAL, indicatorPos[0], indicatorPos[1]);
                    }
                    ctx.restore();
                }
            }
        }
    }

    // ******************** Event Handling ************************************
    public final ObjectProperty<EventHandler<ValidationEvent>> onClearProperty() {
        return onClear;
    }

    public final void setOnClear(EventHandler<ValidationEvent> value) {
        onClearProperty().set(value);
    }

    public final EventHandler<ValidationEvent> getOnClear() {
        return onClearProperty().get();
    }
    private ObjectProperty<EventHandler<ValidationEvent>> onClear = new ObjectPropertyBase<EventHandler<ValidationEvent>>() {
        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "onClear";
        }
    };

    public final ObjectProperty<EventHandler<ValidationEvent>> onOptionalProperty() {
        return onOptional;
    }

    public final void setOnOptional(EventHandler<ValidationEvent> value) {
        onOptionalProperty().set(value);
    }

    public final EventHandler<ValidationEvent> getOnOptional() {
        return onOptionalProperty().get();
    }
    private ObjectProperty<EventHandler<ValidationEvent>> onOptional = new ObjectPropertyBase<EventHandler<ValidationEvent>>() {
        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "onOptional";
        }
    };

    public final ObjectProperty<EventHandler<ValidationEvent>> onInfoProperty() {
        return onInfo;
    }

    public final void setOnInfo(EventHandler<ValidationEvent> value) {
        onInfoProperty().set(value);
    }

    public final EventHandler<ValidationEvent> getOnInfo() {
        return onInfoProperty().get();
    }
    private ObjectProperty<EventHandler<ValidationEvent>> onInfo = new ObjectPropertyBase<EventHandler<ValidationEvent>>() {
        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "onInfo";
        }
    };

    public final ObjectProperty<EventHandler<ValidationEvent>> onValidProperty() {
        return onValid;
    }

    public final void setOnValid(EventHandler<ValidationEvent> value) {
        onValidProperty().set(value);
    }

    public final EventHandler<ValidationEvent> getOnValid() {
        return onValidProperty().get();
    }
    private ObjectProperty<EventHandler<ValidationEvent>> onValid = new ObjectPropertyBase<EventHandler<ValidationEvent>>() {
        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "onValid";
        }
    };

    public final ObjectProperty<EventHandler<ValidationEvent>> onInvalidProperty() {
        return onInvalid;
    }

    public final void setOnInvalid(EventHandler<ValidationEvent> value) {
        onInvalidProperty().set(value);
    }

    public final EventHandler<ValidationEvent> getOnInvalid() {
        return onInvalidProperty().get();
    }
    private ObjectProperty<EventHandler<ValidationEvent>> onInvalid = new ObjectPropertyBase<EventHandler<ValidationEvent>>() {
        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "onInvalid";
        }
    };

    public void fireValidationEvent(final ValidationEvent EVENT) {
        final EventType TYPE = EVENT.getEventType();
        final EventHandler<ValidationEvent> HANDLER;
        if (ValidationEvent.VALID == TYPE) {
            HANDLER = getOnValid();
        } else if (ValidationEvent.INVALID == TYPE) {
            HANDLER = getOnInvalid();
        } else if (ValidationEvent.INFO == TYPE) {
            HANDLER = getOnInfo();
        } else if (ValidationEvent.OPTIONAL == TYPE) {
            HANDLER = getOnOptional();
        } else if (ValidationEvent.CLEAR == TYPE) {
            HANDLER = getOnClear();
        } else {
            HANDLER = null;
        }

        if (HANDLER != null) {
            HANDLER.handle(EVENT);
        }
    }

    // ******************** Inner Classes *************************************
    public static class ValidationEvent extends Event {

        public static final EventType<ValidationEvent> VALID = new EventType(ANY, "valid");
        public static final EventType<ValidationEvent> INVALID = new EventType(ANY, "invalid");
        public static final EventType<ValidationEvent> INFO = new EventType(ANY, "info");
        public static final EventType<ValidationEvent> OPTIONAL = new EventType(ANY, "optional");
        public static final EventType<ValidationEvent> CLEAR = new EventType(ANY, "clear");
        private Node node;

        // ******************* Constructors ***************************************
        public ValidationEvent(final Node NODE, final Object SOURCE, final EventTarget TARGET, final EventType<ValidationEvent> EVENT_TYPE) {
            super(SOURCE, TARGET, EVENT_TYPE);
            node = NODE;
        }

        // ******************* Methods ****************************************
        public Node getNode() {
            return node;
        }
    }
}
