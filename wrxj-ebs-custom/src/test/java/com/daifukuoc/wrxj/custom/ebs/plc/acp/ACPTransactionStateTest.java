package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPTransactionState.Events;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPTransactionState.States;

class ACPTransactionStateTest {

    private static final String PORT_NAME = "ACP1-Port";

    ACPTransactionState transactionState;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        transactionState = new ACPTransactionState(PORT_NAME, logger);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void shouldRepeatCanSendStateAfterGettingAck() throws Exception {
        StateMachineTestPlan<States, Events> plan = StateMachineTestPlanBuilder.<States, Events> builder()
                .defaultAwaitTime(2)
                .stateMachine(transactionState.getMachine())
                .step()
                .expectStates(States.NOT_CONNECTED)
                .and()
                .step()
                .sendEvent(Events.CONNECT)
                .expectStateChanged(1)
                .expectStates(States.CONNECTED)
                .and()
                .step()
                .sendEvent(Events.START_INITIALISATION)
                .expectStateChanged(1)
                .expectStates(States.INITIALISING)
                .and()
                .step()
                .sendEvent(Events.SEND)
                .expectStateChanged(1)
                .expectStates(States.CAN_SEND)
                .and()
                .step()
                .sendEvent(Events.START_REQUEST)
                .expectStateChanged(1)
                .expectStates(States.A_REQUEST_IS_SENT)
                .and()
                .step()
                .sendEvent(Events.SEND)
                .expectStateChanged(1)
                .expectStates(States.CAN_SEND)
                .and()
                .step()
                .sendEvent(Events.START_REQUEST)
                .expectStateChanged(1)
                .expectStates(States.A_REQUEST_IS_SENT)
                .and()
                .step()
                .sendEvent(Events.SEND)
                .expectStateChanged(1)
                .expectStates(States.CAN_SEND)
                .and()
                .build();
        plan.test();
    }

    @Test
    void shouldChangeToDisconnectedFromAllStatesWhenDisconnectEventIsPublished() throws Exception {
        // NOT_CONNECTED ---(DISCONNECT)--> NOT_DISCONNECTED
        StateMachineTestPlanBuilder.<States, Events> builder()
                .defaultAwaitTime(2)
                .stateMachine(transactionState.getMachine())
                .step()
                .expectStates(States.NOT_CONNECTED)
                .and()
                .step()
                .sendEvent(Events.DISCONNECT)
                .expectStateChanged(1)
                .expectStates(States.NOT_CONNECTED)
                .and()
                .build()
                .test();

        // CONNECTED ---(DISCONNECT)--> NOT_DISCONNECTED
        StateMachineTestPlanBuilder.<States, Events> builder()
                .defaultAwaitTime(2)
                .stateMachine(transactionState.getMachine())
                .step()
                .expectStates(States.NOT_CONNECTED)
                .and()
                .step()
                .sendEvent(Events.CONNECT)
                .expectStateChanged(1)
                .expectStates(States.CONNECTED)
                .and()
                .step()
                .sendEvent(Events.DISCONNECT)
                .expectStateChanged(1)
                .expectStates(States.NOT_CONNECTED)
                .and()
                .build()
                .test();
        
        // INITIALISING ---(DISCONNECT)--> NOT_DISCONNECTED
        StateMachineTestPlanBuilder.<States, Events> builder()
                .defaultAwaitTime(2)
                .stateMachine(transactionState.getMachine())
                .step()
                .expectStates(States.NOT_CONNECTED)
                .and()
                .step()
                .sendEvent(Events.CONNECT)
                .expectStateChanged(1)
                .expectStates(States.CONNECTED)
                .and()
                .step()
                .sendEvent(Events.START_INITIALISATION)
                .expectStateChanged(1)
                .expectStates(States.INITIALISING)
                .and()
                .step()
                .sendEvent(Events.DISCONNECT)
                .expectStateChanged(1)
                .expectStates(States.NOT_CONNECTED)
                .and()
                .build()
                .test();
        
        // CAN_SEND ---(DISCONNECT)--> NOT_DISCONNECTED
        StateMachineTestPlanBuilder.<States, Events> builder()
                .defaultAwaitTime(2)
                .stateMachine(transactionState.getMachine())
                .step()
                .expectStates(States.NOT_CONNECTED)
                .and()
                .step()
                .sendEvent(Events.CONNECT)
                .expectStateChanged(1)
                .expectStates(States.CONNECTED)
                .and()
                .step()
                .sendEvent(Events.START_INITIALISATION)
                .expectStateChanged(1)
                .expectStates(States.INITIALISING)
                .and()
                .step()
                .sendEvent(Events.SEND)
                .expectStateChanged(1)
                .expectStates(States.CAN_SEND)
                .and()
                .step()
                .sendEvent(Events.DISCONNECT)
                .expectStateChanged(1)
                .expectStates(States.NOT_CONNECTED)
                .and()
                .build()
                .test();
        
        // A_REQUEST_IS_SENT ---(DISCONNECT)--> NOT_DISCONNECTED
        StateMachineTestPlanBuilder.<States, Events> builder()
                .defaultAwaitTime(2)
                .stateMachine(transactionState.getMachine())
                .step()
                .expectStates(States.NOT_CONNECTED)
                .and()
                .step()
                .sendEvent(Events.CONNECT)
                .expectStateChanged(1)
                .expectStates(States.CONNECTED)
                .and()
                .step()
                .sendEvent(Events.START_INITIALISATION)
                .expectStateChanged(1)
                .expectStates(States.INITIALISING)
                .and()
                .step()
                .sendEvent(Events.SEND)
                .expectStateChanged(1)
                .expectStates(States.CAN_SEND)
                .and()
                .step()
                .sendEvent(Events.START_REQUEST)
                .expectStateChanged(1)
                .expectStates(States.A_REQUEST_IS_SENT)
                .and()
                .step()
                .sendEvent(Events.DISCONNECT)
                .expectStateChanged(1)
                .expectStates(States.NOT_CONNECTED)
                .and()
                .build()
                .test();
    }
    
    @Test
    void shouldExpectedStatusWhenMetthodsAreCalled() throws Exception {
        // When not connected, all of methods return false
        assertFalse(transactionState.shouldStartInitialisation());
        assertFalse(transactionState.canSendRequest());
        assertFalse(transactionState.shouldWaitForAck());
        assertFalse(transactionState.canSendReply());

        // Simulate the execution of connect() which is called after connection is established
        transactionState.connect();
        
        // Connection is established, so it's necessary to do initialisation process
        // - Only can send outbound link start up
        assertTrue(transactionState.shouldStartInitialisation());
        // - Other requests shouldn't be sent
        assertFalse(transactionState.canSendRequest());
        // - Not waiting for ack yet
        assertFalse(transactionState.shouldWaitForAck());
        // - It's allowed to send ack
        assertTrue(transactionState.canSendReply());
        
        // Simulate the execution of startInitialisation() which is called after sending outbound link start up
        transactionState.startInitialisation();
        
        // Outbound link start up is sent, so it's necessary to wait for an outbound link start up ack
        /// - Outbound link start up is already sent
        assertFalse(transactionState.shouldStartInitialisation());
        // - Other requests shouldn't be sent
        assertFalse(transactionState.canSendRequest());
        // - Waiting for ack
        assertTrue(transactionState.shouldWaitForAck());
        // - It's allowed to send ack
        assertTrue(transactionState.canSendReply());
        
        // Simulate the execution of finishInitialisation() which is called after outbound link start up ack is received
        transactionState.finishInitialisation();
        
        // Outbound link start up is acked, so it's possible to send a request
        // - Initialisation has completed
        assertFalse(transactionState.shouldStartInitialisation());
        // - Other requests can be sent
        assertTrue(transactionState.canSendRequest());
        // - Not waiting for an ack yet
        assertFalse(transactionState.shouldWaitForAck());
        // - It's allowed to send ack
        assertTrue(transactionState.canSendReply());
        
        // Simulate the execution of startRequest() which is called after sending request
        transactionState.startRequest();
        
        // A request is sent out
        // - Initialisation has completed
        assertFalse(transactionState.shouldStartInitialisation());
        // - A request is sent out
        assertFalse(transactionState.canSendRequest());
        // - Waiting for ack
        assertTrue(transactionState.shouldWaitForAck());
        // - It's allowed to send ack
        assertTrue(transactionState.canSendReply());
        
        // Simulate the execution of finishRequest() which is called after ack for the pending request is received
        transactionState.finishRequest();
        
        // The request is acked, so it's possible to send another request
        // - Initialisation has completed
        assertFalse(transactionState.shouldStartInitialisation());
        // - Another request can be sent
        assertTrue(transactionState.canSendRequest());
        // - Not waiting for an ack yet
        assertFalse(transactionState.shouldWaitForAck());
        // - It's allowed to send ack
        assertTrue(transactionState.canSendReply());
    }
    
    @Test
    void shouldReturnFalseWhenDisconnected() throws Exception {
        // When not connected, all of methods return false
        assertFalse(transactionState.shouldStartInitialisation());
        assertFalse(transactionState.canSendRequest());
        assertFalse(transactionState.shouldWaitForAck());
        assertFalse(transactionState.canSendReply());

        // Simulate the execution of connect() which is called after connection is established
        transactionState.connect();
        
        // Connection is established, so it's necessary to do initialisation process
        // - Only can send outbound link start up
        assertTrue(transactionState.shouldStartInitialisation());
        // - Other requests shouldn't be sent
        assertFalse(transactionState.canSendRequest());
        // - Not waiting for ack yet
        assertFalse(transactionState.shouldWaitForAck());
        // - It's allowed to send ack
        assertTrue(transactionState.canSendReply());
        
        // Simulate the execution of disconnect() which is called after connection is closed
        transactionState.disconnect();
        
        // When not connected, all of methods return false
        assertFalse(transactionState.shouldStartInitialisation());
        assertFalse(transactionState.canSendRequest());
        assertFalse(transactionState.shouldWaitForAck());
        assertFalse(transactionState.canSendReply());
    }
}
