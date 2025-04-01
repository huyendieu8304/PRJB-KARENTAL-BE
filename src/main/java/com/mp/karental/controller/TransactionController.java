package com.mp.karental.controller;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.dto.request.transaction.TransactionRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.auth.LoginResponse;
import com.mp.karental.dto.response.transaction.ListTransactionResponse;
import com.mp.karental.dto.response.transaction.TransactionPaymentURLResponse;
import com.mp.karental.dto.response.transaction.TransactionResponse;
import com.mp.karental.service.TransactionService;
import com.mp.karental.util.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(value = "/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@Tag(name = "Transaction", description = "API for managing transaction")
public class TransactionController {
    TransactionService transactionService;

    @Operation(
            summary = "Get all transactions",
            description = "Retrieve all transactions within a specified date range",
            parameters = {
                    @Parameter(name = "from", description = "Start date and time (ISO format)", required = true,
                            example = "2024-01-01T00:00:00"),
                    @Parameter(name = "to", description = "End date and time (ISO format)", required = true,
                            example = "2024-12-31T23:59:59")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "1000")),
                                            @SchemaProperty(name = "message", schema = @Schema(type = "string", example = "Success")),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(implementation = ListTransactionResponse.class,
                                                            example = """
                                                            {
                                                                "balance": 100000000,
                                                                "listTransactionResponse": [
                                                                    {
                                                                        "createdAt": "2024-03-20T10:00:00",
                                                                        "type": "TOP_UP",
                                                                        "bookingNo": "BK202410200001",
                                                                        "carName": "Toyota Camry",
                                                                        "amount": 1000000,
                                                                        "message": "Top up successful",
                                                                        "status": "COMPLETED"
                                                                    },
                                                                    {
                                                                        "createdAt": "2024-03-19T15:30:00",
                                                                        "type": "WITHDRAW",
                                                                        "bookingNo": null,
                                                                        "carName": null,
                                                                        "amount": 500000,
                                                                        "message": "Withdrawal successful",
                                                                        "status": "COMPLETED"
                                                                    }
                                                                ]
                                                            }
                                                            """)
                                            )
                                    }
                            )
                    ),

                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = """
                                    Unauthorized
                                    |code  | message |
                                    |------|-------------|
                                    | 4003 | Unauthenticated access. The access token is invalid|
                                    | 4005 | The access token is expired. Please try again|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4004 | User doesn't have permission to access the endpoint.|
                                    | 4006 | Your account is inactive.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @GetMapping(value="/transaction-list", params = { "from", "to" })
    public ApiResponse<ListTransactionResponse> getAllTransactionResponseList(@RequestParam(name = "from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                              @RequestParam(name = "to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){
        return ApiResponse.<ListTransactionResponse>builder()
                .data(transactionService.getAllTransactions(from,to))
                .build();
    }
    @Operation(
            summary = "Get all transactions (complete list)",
            description = "Retrieve all transactions without date filtering",
            parameters = {
                    @Parameter(name = "all", description = "Set to true to get all transactions", required = true,
                            example = "true")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "1000")),
                                            @SchemaProperty(name = "message", schema = @Schema(type = "string", example = "Success")),
                                            @SchemaProperty(
                                                    name = "data",
                                                    schema = @Schema(implementation = ListTransactionResponse.class,
                                                            example = """
                                                            {
                                                                "balance": 100000000,
                                                                "listTransactionResponse": [
                                                                    {
                                                                        "createdAt": "2024-03-20T10:00:00",
                                                                        "type": "TOP_UP",
                                                                        "bookingNo": "BK202410200001",
                                                                        "carName": "Toyota Camry",
                                                                        "amount": 1000000,
                                                                        "message": "Top up successful",
                                                                        "status": "COMPLETED"
                                                                    }
                                                                ]
                                                            }
                                                            """)
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = """
                                    Unauthorized
                                    |code  | message |
                                    |------|-------------|
                                    | 4003 | Unauthenticated access. The access token is invalid|
                                    | 4005 | The access token is expired. Please try again|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4004 | User doesn't have permission to access the endpoint.|
                                    | 4006 | Your account is inactive.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @GetMapping(value="/transaction-list", params = "all")
    public ApiResponse<ListTransactionResponse> getAllTransactionList(@RequestParam boolean all){
        if (Boolean.TRUE.equals(all)) {
        return ApiResponse.<ListTransactionResponse>builder()
                .data(transactionService.getAllTransactionsList())
                .build();
    }
    return ApiResponse.<ListTransactionResponse>builder()
            .data(new ListTransactionResponse()) // Return empty list if `all` is false or null
            .build();

    }
    @Operation(
            summary = "Top up wallet",
            description = "Add funds to user's wallet",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "1000")),
                                            @SchemaProperty(name = "message", schema = @Schema(type = "string", example = "Success")),
                                            @SchemaProperty(name = "data", schema = @Schema(implementation = TransactionPaymentURLResponse.class))
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad request
                                    |code  | message |
                                    |------|-------------|
                                    | 3011 | Your information is wrong.|
                                    | 3012 | VNPAY Checksum sequence has error.|
                                    | 3013 | Payment failed by some reasons.|
                                    | 3015 | The wallet is not exist in the system|
                                    
                                    """
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = """
                                    Unauthorized
                                    |code  | message |
                                    |------|-------------|
                                    | 4003 | Unauthenticated access. The access token is invalid|
                                    | 4005 | The access token is expired. Please try again|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4004 | User doesn't have permission to access the endpoint.|
                                    | 4006 | Your account is inactive.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @PostMapping("/top-up")
    public ApiResponse<TransactionPaymentURLResponse> topUp(@RequestBody @Valid TransactionRequest transactionRequest, HttpServletRequest httpServletRequest) {
        var ipAddress = RequestUtil.getIpAddress(httpServletRequest);
        transactionRequest.setIpAddress(ipAddress);
        log.info("Transaction Request: {}", transactionRequest);
        transactionRequest.setType(ETransactionType.TOP_UP);
        return ApiResponse.<TransactionPaymentURLResponse>builder()
                .data(transactionService.createTransactionTopUp(transactionRequest))
                .build();
    }
    @Operation(
            summary = "Withdraw from wallet",
            description = "Withdraw funds from user's wallet",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "1000")),
                                            @SchemaProperty(name = "message", schema = @Schema(type = "string", example = "Success")),
                                            @SchemaProperty(name = "data", schema = @Schema(implementation = TransactionResponse.class))
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad request
                                    |code  | message |
                                    |------|-------------|
                                    | 3014 | Amount is exceeded wallet balance.|
                                    | 3015 | The wallet is not exist in the system|
                                    
                                    """
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = """
                                    Unauthorized
                                    |code  | message |
                                    |------|-------------|
                                    | 4003 | Unauthenticated access. The access token is invalid|
                                    | 4005 | The access token is expired. Please try again|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4004 | User doesn't have permission to access the endpoint.|
                                    | 4006 | Your account is inactive.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @PostMapping("/withdraw")
    public ApiResponse<TransactionResponse> withdraw(@RequestBody @Valid TransactionRequest transactionRequest, HttpServletRequest httpServletRequest) {
        var ipAddress = RequestUtil.getIpAddress(httpServletRequest);
        transactionRequest.setIpAddress(ipAddress);
        log.info("Transaction Request: {}", transactionRequest);
        transactionRequest.setType(ETransactionType.WITHDRAW);
        return ApiResponse.<TransactionResponse>builder()
                .data(transactionService.withdraw(transactionRequest.getAmount()))
                .build();
    }
    @Operation(
            summary = "Get transaction status",
            description = "Check the status of a specific transaction",
            parameters = {
                    @Parameter(name = "transactionId", description = "Transaction ID to check", required = true,
                            example = "TR202401010001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    schema = @Schema(type = "object"),
                                    schemaProperties = {
                                            @SchemaProperty(name = "code", schema = @Schema(type = "string", example = "1000")),
                                            @SchemaProperty(name = "message", schema = @Schema(type = "string", example = "Success")),
                                            @SchemaProperty(name = "data", schema = @Schema(implementation = TransactionResponse.class))
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = """
                                    Unauthorized
                                    |code  | message |
                                    |------|-------------|
                                    | 4003 | Unauthenticated access. The access token is invalid|
                                    | 4005 | The access token is expired. Please try again|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = """
                                    Forbidden
                                    |code  | message |
                                    |------|-------------|
                                    | 4004 | User doesn't have permission to access the endpoint.|
                                    | 4006 | Your account is inactive.|
                                    """,
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    )
            }
    )
    @GetMapping("{transactionId}/status")
    public ApiResponse<TransactionResponse> getTransaction(@PathVariable String transactionId, @RequestParam Map<String, String> params) {
            return ApiResponse.<TransactionResponse>builder()
                    .data(transactionService.getTransactionStatus(transactionId, params))
                    .build();
    }


}
