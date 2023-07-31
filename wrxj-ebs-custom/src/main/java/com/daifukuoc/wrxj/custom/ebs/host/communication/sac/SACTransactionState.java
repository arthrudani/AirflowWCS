package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPTransactionState;


import reactor.core.publisher.Mono;

public class SACTransactionState {
	enum States {
        NOT_CONNECTED, CONNECTED, INITIALISING, CAN_SEND, A_REQUEST_IS_SENT
    }

    enum Events {
        DISCONNECT, CONNECT, START_INITIALISATION, SEND, START_REQUEST
    }

    private final String portName;
    private final Logger logger;
    private final Builder<States, Events> builder;
    private final StateMachine<States, Events> machine;

    public SACTransactionState(String portName, Logger logger) throws Exception {
        this.portName = portName;
        this.logger = logger;

        builder = StateMachineBuilder.builder();

        builder.configureConfiguration().withConfiguration()
                .autoStartup(true);

        builder.configureStates().withStates()
                .initial(States.NOT_CONNECTED)
                .state(States.CONNECTED)
                .state(States.INITIALISING)
                .state(States.A_REQUEST_IS_SENT)
                .state(States.CAN_SEND);

        builder.configureTransitions()
                // Normal transitions
                .withExternal()
                .source(States.NOT_CONNECTED)
                .event(Events.CONNECT)
                .target(States.CONNECTED)
                .and()
                .withExternal()
                .source(States.CONNECTED)
                .event(Events.START_INITIALISATION)
                .target(States.INITIALISING)
                .and()
                .withExternal()
                .source(States.INITIALISING)
                .event(Events.SEND)
                .target(States.CAN_SEND)
                .and()
                .withExternal()
                .source(States.CAN_SEND)
                .event(Events.START_REQUEST)
                .target(States.A_REQUEST_IS_SENT)
                .and()
                .withExternal()
                .source(States.A_REQUEST_IS_SENT)
                .event(Events.SEND)
                .target(States.CAN_SEND)
                .and()

                // Disconnected
                .withExternal()
                .source(States.NOT_CONNECTED)
                .event(Events.DISCONNECT)
                .target(States.NOT_CONNECTED)
                .and()
                .withExternal()
                .source(States.CONNECTED)
                .event(Events.DISCONNECT)
                .target(States.NOT_CONNECTED)
                .and()
                .withExternal()
                .source(States.INITIALISING)
                .event(Events.DISCONNECT)
                .target(States.NOT_CONNECTED)
                .and()
                .withExternal()
                .source(States.CAN_SEND)
                .event(Events.DISCONNECT)
                .target(States.NOT_CONNECTED)
                .and()
                .withExternal()
                .source(States.A_REQUEST_IS_SENT)
                .event(Events.DISCONNECT)
                .target(States.NOT_CONNECTED);

        machine = builder.build();

        machine.addStateListener(new StateMachineListener<SACTransactionState.States, SACTransactionState.Events>() {
            @Override
            public void transitionStarted(Transition<States, Events> transition) {
            }

            @Override
            public void transitionEnded(Transition<States, Events> transition) {
            }

            @Override
            public void transition(Transition<States, Events> transition) {
            }

            @Override
            public void stateMachineStopped(StateMachine<States, Events> stateMachine) {
            }

            @Override
            public void stateMachineStarted(StateMachine<States, Events> stateMachine) {
            }

            @Override
            public void stateMachineError(StateMachine<States, Events> stateMachine, Exception exception) {
                logger.logException(portName + "'s transaction state: error", exception);
            }

            @Override
            public void stateExited(State<States, Events> state) {
            }

            @Override
            public void stateEntered(State<States, Events> state) {
            }

            @Override
            public void stateContext(StateContext<States, Events> stateContext) {
            }

            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                logger.logDebug(portName + "'s transaction state: " + from.getId() + " --> " + to.getId());
            }

            @Override
            public void extendedStateChanged(Object key, Object value) {
            }

            @Override
            public void eventNotAccepted(Message<Events> event) {
                logger.logError(portName + "'s transaction state: " + event + " was not accepted");
            }
        });
    }

    /**
     * Use this only for unit tests
     * 
     * @return state machine
     */
    public StateMachine<States, Events> getMachine() {
        return machine;
    }

    public void connect() {
        sendEvent(Events.CONNECT);
    }

    public void disconnect() {
        sendEvent(Events.DISCONNECT);
    }

    public void startInitialisation() {
        sendEvent(Events.START_INITIALISATION);
    }

    public void finishInitialisation() {
        sendEvent(Events.SEND);
    }

    public void startRequest() {
        sendEvent(Events.START_REQUEST);
    }

    public void finishRequest() {
        sendEvent(Events.SEND);
    }

    public boolean shouldStartInitialisation() {
        return machine.getState().getId().equals(States.CONNECTED);
    }

    public boolean canSendRequest() {
        return machine.getState().getId().equals(States.CAN_SEND);
    }

    public boolean shouldWaitForAck() {
        switch (machine.getState().getId()) {
        case INITIALISING:
        case A_REQUEST_IS_SENT:
            return true;
        default:
            break;
        }

        return false;
    }

    public boolean canSendReply() {
        switch (machine.getState().getId()) {
        case CONNECTED:
        case INITIALISING:
        case CAN_SEND:
        case A_REQUEST_IS_SENT:
            return true;
        default:
            break;
        }

        return false;
    }

    private StateMachineEventResult<States, Events> sendEvent(Events eventToSend) {
        CompletableFuture<StateMachineEventResult<States, Events>> future = Mono
                .from(machine.sendEvent(Mono.just(MessageBuilder.withPayload(eventToSend).build()))).toFuture();
        try {
            StateMachineEventResult<States, Events> result = future.get();
            return result;
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
}
