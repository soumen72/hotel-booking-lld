package com.hbooking.api;

import com.hbooking.dto.response.BookingResponse;
import com.hbooking.dto.response.CancelBookingResponse;
import com.hbooking.dto.request.CreateBookingRequest;
import com.hbooking.dto.request.PaymentRequest;
import com.hbooking.dto.response.PaymentResponse;
import com.hbooking.model.Booking;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.payment.PaymentMethod;
import com.hbooking.payment.PaymentReceipt;
import com.hbooking.payment.impl.CardPayment;
import com.hbooking.payment.impl.UpiPayment;
import com.hbooking.payment.impl.WalletPayment;
import com.hbooking.service.BookingService;
import com.hbooking.model.CancellationResult;
import com.hbooking.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        DateRange dateRange = DateRange.of(request.getCheckIn(), request.getCheckOut());
        GuestCount guestCount = GuestCount.of(request.getAdults(), request.getChildren());

        Booking booking = bookingService.createBooking(
                request.getPropertyId(),
                request.getRoomTypeId(),
                request.getGuestId(),
                dateRange,
                guestCount);

        return ResponseEntity.status(HttpStatus.CREATED).body(new BookingResponse(booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable("id") String id) {
        Booking booking = bookingService.getBooking(id);
        return ResponseEntity.ok(new BookingResponse(booking));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResponse> payBooking(
            @PathVariable("id") String bookingId,
            @RequestBody PaymentRequest request) {

        PaymentMethod method = resolvePaymentMethod(request);
        PaymentReceipt receipt = paymentService.processPayment(bookingId, request.getIdempotencyKey(), method);
        Booking updatedBooking = bookingService.getBooking(bookingId);

        HttpStatus status = receipt.isSuccessful() ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(new PaymentResponse(receipt, updatedBooking.getStatus().name()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<CancelBookingResponse> cancelBooking(@PathVariable("id") String bookingId) {
        CancellationResult result = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(new CancelBookingResponse(result.booking(), result.refundAmount()));
    }

    private PaymentMethod resolvePaymentMethod(PaymentRequest req) {
        String type = req.getPaymentMethod() != null ? req.getPaymentMethod().toUpperCase() : "CARD";
        return switch (type) {
            case "CARD" -> CardPayment.builder()
                    .cardNumber(req.getCardNumber())
                    .cardHolderName(req.getCardHolderName())
                    .expiry(req.getExpiry())
                    .cvv(req.getCvv())
                    .build();
            case "UPI" -> UpiPayment.builder()
                    .upiId(req.getUpiId())
                    .build();
            case "WALLET" -> WalletPayment.builder()
                    .walletProvider(req.getWalletProvider())
                    .accountId(req.getWalletAccountId())
                    .build();
            default -> throw new IllegalArgumentException("Unsupported payment method: " + req.getPaymentMethod());
        };
    }
}
