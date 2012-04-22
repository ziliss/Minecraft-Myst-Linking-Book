package net.minecraft.src.mystlinkingbook;

import java.util.concurrent.ExecutionException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

/**
 * 
 * @author ziliss
 * @since 0.9b
 */
public abstract class SynchronousThread extends Thread {
	
	protected final Object sync = new Object();
	
	protected volatile State state = State.NEW;
	protected volatile long continueUntil = 0;
	protected boolean needsOpenGLContext = false; // Always accessed in synchronized blocks.
	protected boolean hasOpenGLContext = false; // Only local to the thread
	protected volatile boolean aborted = false;
	protected Throwable exception = null; // Always accessed in synchronized blocks.
	
	public SynchronousThread() {
	}
	
	public SynchronousThread(String threadName) {
		super(threadName);
	}
	
	public SynchronousThread(State firstRunningState) {
	}
	
	public SynchronousThread(String threadName, State firstRunningState) {
		super(threadName);
	}
	
	@Override
	public void run() {
		Throwable throwable = null;
		try {
			syncRun();
		}
		catch (AbortedException e) {
		}
		catch (Exception e) {
			throwable = e;
		}
		synchronized (sync) {
			if (throwable == null) {
				state = State.FINISHED;
			}
			else {
				exception = throwable;
				state = State.ERROR;
			}
			releaseOpenGLThreadContext();
			hasOpenGLContext = false;
			this.needsOpenGLContext = false;
			sync.notifyAll();
		}
	}
	
	public abstract void syncRun() throws AbortedException, Exception;
	
	protected void checkContinue() throws AbortedException {
		checkContinue(false);
	}
	
	protected void checkContinue(boolean needsOpenGLContext) throws AbortedException {
		if (aborted) throw new AbortedException();
		
		if (!checkContinueCondition(needsOpenGLContext) || System.currentTimeMillis() >= continueUntil) {
			pause(State.RUNNING, needsOpenGLContext);
		}
	}
	
	protected void checkContinueASync() throws AbortedException {
		if (aborted) throw new AbortedException();
		
		if (hasOpenGLContext) {
			releaseOpenGLThreadContext();
			hasOpenGLContext = false;
		}
		
		if (!checkContinueASyncCondition()) {
			pause(State.RUNNING_ASYNC, false);
		}
		else {
			sync.notify();
		}
	}
	
	protected void pause(State nextRunningState, boolean needsOpenGLContext) throws AbortedException {
		synchronized (sync) {
			if (hasOpenGLContext) {
				releaseOpenGLThreadContext();
				hasOpenGLContext = false;
			}
			this.needsOpenGLContext = needsOpenGLContext;
			do {
				state = State.WAITING;
				sync.notify();
				do {
					try {
						sync.wait();
						if (aborted) throw new AbortedException();
					}
					catch (InterruptedException e) {
					}
				} while (state != State.CONTINUE);
				if (needsOpenGLContext) {
					hasOpenGLContext = acquireOpenGLThreadContext();
				}
			} while (needsOpenGLContext && !hasOpenGLContext);
			state = nextRunningState;
			if (nextRunningState == State.RUNNING_ASYNC) {
				sync.notify();
			}
		}
	}
	
	protected boolean acquireOpenGLThreadContext() {
		try {
			Display.makeCurrent();
			return true;
		}
		catch (LWJGLException e) {
		}
		return false;
	}
	
	protected static boolean releaseOpenGLThreadContext() {
		try {
			Display.releaseContext();
			return true;
		}
		catch (LWJGLException e) {
		}
		return false;
	}
	
	public boolean checkContinueCondition(boolean needsOpenGLContext) {
		return true;
	}
	
	public boolean checkContinueASyncCondition() {
		return true;
	}
	
	public boolean continueExecOnce() throws ExecutionException {
		return continueExecDuring(0);
	}
	
	public boolean continueExecDuring(long continueDuring) throws ExecutionException {
		return continueExecUntil(System.currentTimeMillis() + continueDuring);
	}
	
	public boolean continueExecUntil(long continueUntil) throws ExecutionException {
		if (hasFinished()) return false;
		
		synchronized (sync) {
			if (state.isRunningState()) {
				if (getState() == Thread.State.NEW) {
					start();
				}
				long waitLeft = continueUntil - System.currentTimeMillis();
				while (state.isRunningState() && waitLeft > 0) {
					try {
						sync.wait(waitLeft);
					}
					catch (InterruptedException e) {
					}
					waitLeft = continueUntil - System.currentTimeMillis();
				}
				if (waitLeft < 0) return state != State.FINISHED;
			}
			
			// Here state is WAITING or ERROR or FINISHED.
			
			if (state == State.WAITING) {
				boolean releasedOpenGLContext = false;
				if (needsOpenGLContext) {
					releasedOpenGLContext = releaseOpenGLThreadContext();
					if (!releasedOpenGLContext) return true;
				}
				this.continueUntil = continueUntil;
				setPriority(Thread.currentThread().getPriority());
				state = State.CONTINUE;
				sync.notify();
				do {
					try {
						sync.wait();
					}
					catch (InterruptedException e) {
					}
				} while (state.isSyncRunningState());
				if (releasedOpenGLContext) {
					acquireOpenGLThreadContext();
				}
			}
			if (state == State.ERROR) {
				state = State.FINISHED;
				throw new ExecutionException(exception);
			}
		}
		return state != State.FINISHED;
	}
	
	public void abort() {
		aborted = true;
	}
	
	public void finishExec() throws ExecutionException {
		while (continueExecUntil(Long.MAX_VALUE)) {
		}
	}
	
	public State getSyncState() {
		return state;
	}
	
	public boolean hasFinished() {
		return state == State.FINISHED;
	}
	
	public static class AbortedException extends Exception {
	}
	
	public static enum State {
		/** Until started */
		NEW,
		
		/**  */
		WAITING,
		
		/**  */
		CONTINUE,
		
		/**  */
		RUNNING,
		
		/**  */
		RUNNING_ASYNC,
		
		/**  */
		ERROR,
		
		/**  */
		FINISHED;
		
		public boolean isRunningState() {
			return this == RUNNING || this == RUNNING_ASYNC || this == CONTINUE || this == NEW;
		}
		
		public boolean isSyncRunningState() {
			return this == RUNNING || this == CONTINUE || this == NEW;
		}
		
		public boolean isASyncRunningState() {
			return this == RUNNING_ASYNC || this == CONTINUE || this == NEW;
		}
	}
}
