package io.bootique.logback.appender;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.logback.policy.FixedWindowPolicyFactory;
import io.bootique.logback.policy.RollingPolicyFactory;
import io.bootique.logback.policy.TimeBasedPolicyFactory;

import java.util.Objects;

/**
 * A configuration object that sets up a file appender in Logback, potentially
 * with support for rotation, etc.
 * 
 * @since 0.8
 */
@JsonTypeName("file")
public class FileAppenderFactory extends AppenderFactory {

	private String file;
	private RollingPolicyFactory rollingPolicy;

	/**
	 * Sets a filename for the current log file.
	 * 
	 * @param file a filename for the current log file.
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @since 0.12
	 * @return name of the log file.
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Rolling policy factory what defines rolling policy for rotation.
	 * If rolling policy factory is not defined the rotation is not used
	 *
	 * @see RollingPolicyFactory
	 * @see FixedWindowPolicyFactory
	 * @see TimeBasedPolicyFactory
	 * @see ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
	 *
	 * @since 0.10
	 * @param rollingPolicy a rolling policy factory
	 */
	public void setRollingPolicy(RollingPolicyFactory rollingPolicy) {
		this.rollingPolicy = rollingPolicy;
	}

	/**
	 * @since 0.12
	 * @return factory for log file rotation policy
	 */
	public RollingPolicyFactory getRollingPolicy() {
		return rollingPolicy;
	}

	@Override
	public Appender<ILoggingEvent> createAppender(LoggerContext context) {

		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
		encoder.setLayout(createLayout(context));

		FileAppender<ILoggingEvent> appender;
		if (rollingPolicy == null) {
			appender = createSingleFileAppender(encoder, context);
		} else {
			appender = createRollingFileAppender(encoder, context, rollingPolicy);
		}
		return asAsync(appender);
	}

	protected FileAppender<ILoggingEvent> createSingleFileAppender(Encoder<ILoggingEvent> encoder,
			LoggerContext context) {
		FileAppender<ILoggingEvent> appender = new FileAppender<>();
		appender.setFile(Objects.requireNonNull(file));

		appender.setContext(context);
		appender.setEncoder(encoder);
		appender.start();

		return appender;
	}

	protected FileAppender<ILoggingEvent> createRollingFileAppender(Encoder<ILoggingEvent> encoder,
																	LoggerContext context,
																	RollingPolicyFactory rollingPolicy) {

		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setFile(file);
		appender.setContext(context);
		appender.setEncoder(encoder);
		// Setup rolling policy
		RollingPolicy policy = rollingPolicy.createRollingPolicy(context);
		appender.setRollingPolicy(policy);
		policy.setParent(appender);
		// Setup triggering policy
		TriggeringPolicy<ILoggingEvent> triggeringPolicy = rollingPolicy.createTriggeringPolicy(context);
		if (triggeringPolicy != null) {
			appender.setTriggeringPolicy(triggeringPolicy);
			triggeringPolicy.start();
		}
		policy.start();
		appender.start();

		return appender;
	}

}
