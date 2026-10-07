/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JFA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lonter.jfa.api.exceptions;

import lonter.jfa.api.requests.ErrorResponse;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.JFALogger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates an unhandled error that is returned by Fluxer API Request using {@link lonter.jfa.api.requests.RestAction RestAction}
 * <br>It holds an {@link lonter.jfa.api.requests.ErrorResponse ErrorResponse}
 *
 * @see lonter.jfa.api.exceptions.ErrorHandler
 */
public class ErrorResponseException extends RuntimeException {
    private final ErrorResponse errorResponse;
    private final Response response;
    private final String meaning;
    private final int code;
    private final List<SchemaError> schemaErrors;

    /**
     * Creates a new ErrorResponseException instance
     *
     * @param errorResponse
     *        The {@link lonter.jfa.api.requests.ErrorResponse ErrorResponse} corresponding
     *        for the received error response from Fluxer
     * @param response
     *        The Fluxer Response causing the ErrorResponse
     */
    private ErrorResponseException(
            ErrorResponse errorResponse, Response response, int code, String meaning, List<SchemaError> schemaErrors) {
        super(code + ": " + meaning
                + (schemaErrors.isEmpty()
                        ? ""
                        : "\n"
                                + schemaErrors.stream()
                                        .map(SchemaError::toString)
                                        .collect(Collectors.joining("\n"))));

        this.response = response;
        if (response != null && response.getException() != null) {
            initCause(response.getException());
        }
        this.errorResponse = errorResponse;
        this.code = code;
        this.meaning = meaning;
        this.schemaErrors = Collections.unmodifiableList(schemaErrors);
    }

    private ErrorResponseException(String message, ErrorResponseException cause) {
        super(cause.code + ": " + message, cause);

        this.response = cause.response;
        this.errorResponse = cause.errorResponse;
        this.code = cause.code;
        this.meaning = cause.meaning;
        this.schemaErrors = cause.schemaErrors;
    }

    /**
     * Whether this is an internal server error from fluxer (status 500)
     *
     * @return True, if this is an internal server error
     *         {@link lonter.jfa.api.requests.ErrorResponse#SERVER_ERROR ErrorResponse.SERVER_ERROR}
     */
    public boolean isServerError() {
        return errorResponse == ErrorResponse.SERVER_ERROR;
    }

    /**
     * The meaning for this error.
     * <br>It is possible that the value from this method is different for {@link #isServerError() server errors}
     *
     * @return Never-null meaning of this error.
     */
    @NotNull
    public String getMeaning() {
        return meaning;
    }

    /**
     * The fluxer error code for this error response.
     *
     * @return The fluxer error code.
     *
     * @see <a href="https://fluxer.com/developers/docs/topics/opcodes-and-status-codes#json-json-error-codes" target="_blank">Fluxer Error Codes</a>
     */
    public int getErrorCode() {
        return code;
    }

    /**
     * The {@link lonter.jfa.api.requests.ErrorResponse ErrorResponse} corresponding
     * for the received error response from Fluxer
     *
     * @return {@link lonter.jfa.api.requests.ErrorResponse ErrorResponse}
     */
    @NotNull
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    /**
     * The Fluxer Response causing the ErrorResponse
     *
     * @return {@link lonter.jfa.api.requests.Response Response}
     */
    @NotNull
    public Response getResponse() {
        return response;
    }

    /**
     * The {@link SchemaError SchemaErrors} for this error response.
     * <br>These errors provide more context of what part in the body caused the error, and more explanation for the error itself.
     *
     * @return Possibly-empty list of {@link SchemaError SchemaError}
     */
    @NotNull
    public List<SchemaError> getSchemaErrors() {
        return schemaErrors;
    }

    @NotNull
    public static ErrorResponseException create(@NotNull String message, @NotNull ErrorResponseException cause) {
        return new ErrorResponseException(message, cause);
    }

    @NotNull
    public static ErrorResponseException create(@NotNull ErrorResponse errorResponse, @NotNull Response response) {
        String meaning = errorResponse.getMeaning();
        int code = errorResponse.getCode();
        List<SchemaError> schemaErrors = new ArrayList<>();
        try {
            Optional<DataObject> optObj = response.optObject();
            if (response.isError() && response.getException() != null) {
                // this generally means that an exception occurred trying to
                // make an http request. e.g.:
                // SocketTimeoutException/ UnknownHostException
                code = response.code;
                meaning = response.getException().getClass().getName();
            } else if (optObj.isPresent()) {
                DataObject obj = optObj.get();
                if (!obj.isNull("code") || !obj.isNull("message")) {
                    if (!obj.isNull("code")) {
                        code = obj.getInt("code");
                    }
                    if (!obj.isNull("message")) {
                        meaning = obj.getString("message");
                    }
                } else {
                    // This means that neither code or message is provided
                    // In that case we simply put the raw response in place!
                    code = response.code;
                    meaning = obj.toString();
                }

                obj.optObject("errors").ifPresent(schema -> parseSchema(schemaErrors, "", schema));
            } else {
                // error response body is not JSON
                code = response.code;
                meaning = response.getString();
            }
        } catch (Exception e) {
            JFALogger.getLog(ErrorResponseException.class)
                    .error("Failed to parse parts of error response. Body: {}", response.getString(), e);
        }

        return new ErrorResponseException(errorResponse, response, code, meaning, schemaErrors);
    }

    private static void parseSchema(List<SchemaError> schemaErrors, String currentLocation, DataObject errors) {
        // check what kind of errors we are dealing with
        for (String name : errors.keys()) {
            if (name.equals("_errors")) {
                schemaErrors.add(parseSchemaError(currentLocation, errors));
                continue;
            }
            DataObject schemaError = errors.getObject(name);
            if (!schemaError.isNull("_errors")) {
                // We are dealing with an Object Error
                schemaErrors.add(parseSchemaError(currentLocation + name, schemaError));
            } else if (schemaError.keys().stream().allMatch(Helpers::isNumeric)) {
                // We have an Array Error
                for (String index : schemaError.keys()) {
                    DataObject properties = schemaError.getObject(index);
                    String location = String.format("%s%s[%s].", currentLocation, name, index);
                    if (properties.hasKey("_errors")) {
                        schemaErrors.add(parseSchemaError(location.substring(0, location.length() - 1), properties));
                    } else {
                        parseSchema(schemaErrors, location, properties);
                    }
                }
            } else {
                // We have a nested schema error, use recursion!
                String location = String.format("%s%s.", currentLocation, name);
                parseSchema(schemaErrors, location, schemaError);
            }
        }
    }

    private static SchemaError parseSchemaError(String location, DataObject obj) {
        List<ErrorCode> codes = obj.getArray("_errors").stream(DataArray::getObject)
                .map(json -> new ErrorCode(json.getString("code"), json.getString("message")))
                .collect(Collectors.toList());
        return new SchemaError(location, codes);
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(EnumSet.of(UNKNOWN_MESSAGE)))
     *     );
     * }
     * }
     *
     * @param  set
     *         Set of ignored error responses
     *
     * @throws IllegalArgumentException
     *         If provided with null or an empty collection
     *
     * @return {@link Consumer} decorator for {@link RestAction#getDefaultFailure()}
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @NotNull
    public static Consumer<Throwable> ignore(@NotNull Collection<ErrorResponse> set) {
        return ignore(RestAction.getDefaultFailure(), set);
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(UNKNOWN_MESSAGE))
     *     );
     * }
     * }
     *
     * @param  ignored
     *         Ignored error response
     * @param  errorResponses
     *         Additional error responses to ignore
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link Consumer} decorator for {@link RestAction#getDefaultFailure()}
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @NotNull
    public static Consumer<Throwable> ignore(@NotNull ErrorResponse ignored, @NotNull ErrorResponse... errorResponses) {
        return ignore(RestAction.getDefaultFailure(), ignored, errorResponses);
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(Throwable::printStackTrace, UNKNOWN_MESSAGE))
     *     );
     * }
     * }
     *
     * @param  orElse
     *         Behavior to default to if the error response is not ignored
     * @param  ignored
     *         Ignored error response
     * @param  errorResponses
     *         Additional error responses to ignore
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link Consumer} decorator for the provided callback
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @NotNull
    public static Consumer<Throwable> ignore(
            @NotNull Consumer<? super Throwable> orElse,
            @NotNull ErrorResponse ignored,
            @NotNull ErrorResponse... errorResponses) {
        return ignore(orElse, EnumSet.of(ignored, errorResponses));
    }

    /**
     * Ignore the specified set of error responses.
     *
     * <p><b>Example</b><br>
     * {@snippet lang="java":
     * // Creates a message with the provided content and deletes it 30 seconds later
     * public static void selfDestruct(MessageChannel channel, String content) {
     *     channel.sendMessage(content).queue((message) ->
     *         message.delete().queueAfter(30, SECONDS, null, ignore(Throwable::printStackTrace, EnumSet.of(UNKNOWN_MESSAGE)))
     *     );
     * }
     * }
     *
     * @param  orElse
     *         Behavior to default to if the error response is not ignored
     * @param  set
     *         Set of ignored error responses
     *
     * @throws IllegalArgumentException
     *         If provided with null or an empty collection
     *
     * @return {@link Consumer} decorator for the provided callback
     *         which ignores the specified {@link ErrorResponse ErrorResponses}
     */
    @NotNull
    public static Consumer<Throwable> ignore(
            @NotNull Consumer<? super Throwable> orElse, @NotNull Collection<ErrorResponse> set) {
        Checks.notNull(orElse, "Callback");
        Checks.notEmpty(set, "Ignored collection");
        // Make an enum set copy (for performance, memory efficiency, and thread-safety)
        EnumSet<ErrorResponse> ignored = EnumSet.copyOf(set);
        return new ErrorHandler(orElse).ignore(ignored);
    }

    /**
     * An error for a {@link SchemaError}.
     * <br>This provides the machine parsable error code name and the human readable message.
     */
    public static class ErrorCode {
        private final String code;
        private final String message;

        ErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * The machine parsable error code
         *
         * @return The error code
         */
        @NotNull
        public String getCode() {
            return code;
        }

        /**
         * The human readable explanation message for this error
         *
         * @return The message
         */
        @NotNull
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + ": " + message;
        }
    }

    /**
     * Schema error which supplies more context to a ErrorResponse.
     * <br>This provides a list of {@link ErrorCode ErrorCodes} and a {@link #getLocation() location} for the errors.
     */
    public static class SchemaError {
        private final String location;
        private final List<ErrorCode> errors;

        private SchemaError(String location, List<ErrorCode> codes) {
            this.location = location;
            this.errors = codes;
        }

        /**
         * The JSON-path for the error.
         * <br>This path describes the location of the error, within the request json body.
         *
         * <p><b>Example:</b> {@code embed.fields[3].name}
         *
         * @return The JSON-path location
         */
        @NotNull
        public String getLocation() {
            return location;
        }

        /**
         * The list of {@link ErrorCode ErrorCodes} associated with this schema error.
         *
         * @return The error codes
         */
        @NotNull
        public List<ErrorCode> getErrors() {
            return errors;
        }

        @Override
        public String toString() {
            return (location.isEmpty() ? "" : location + "\n") + "\t- "
                    + errors.stream().map(Object::toString).collect(Collectors.joining("\n\t- "));
        }
    }
}
