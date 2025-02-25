package com.subscore.api.controller;

import com.subscore.api.dto.SubscriptionDTO;
import com.subscore.api.model.Subscription;
import com.subscore.api.service.SubscriptionService;
import com.subscore.api.service.UserService;
import com.subscore.api.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * サブスクリプション情報を管理するRESTful APIコントローラー
 *
 * <p>このコントローラーは、サブスクリプションの作成、取得、更新、削除の
 * CRUD操作を提供します。全てのエンドポイントは '/api/subscriptions' をベースとします。
 *
 * <p>主な機能：
 * - 全サブスクリプション情報の取得 (GET /api/subscriptions)
 * - 新規サブスクリプションの登録 (POST /api/subscriptions)
 * - サブスクリプション情報の更新 (PUT /api/subscriptions/{id})
 * - サブスクリプションの削除 (DELETE /api/subscriptions/{id})
 * - ユーザーIDに紐づくサブスクリプション情報の取得 (GET /api/subscriptions/user/{userId})
 *
 * @version 1.0.0
 * @see Subscription
 * @see SubscriptionService
 */
@RestController
@RequestMapping("/api/subscriptions")
@Validated
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    /**
     * 全ユーザーの登録してるサブスクリプション情報を取得するAPIエンドポイント
     *
     * <p>登録されている全てのサブスクリプション情報を取得します。
     * データが存在しない場合は204 No Contentを返却します。
     *
     * @return ResponseEntity<List<Subscription>> 以下のいずれかを返却
     *          - 200 OK：サブスクリプションが一件以上存在する場合
     *          - 204 No Content：サブスクリプションが0件の場合
     */
    @GetMapping
    public ResponseEntity<List<Subscription>> getAll() {
        List<Subscription> subscriptions = subscriptionService.getSubscriptionAll();
        return subscriptions.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(subscriptions);
    }

    /**
     * 新規サブスクリプション情報を登録するAPIエンドポイント
     *
     * @param subscription 登録するサブスクリプション情報
     * @return ResponseEntity<Subscription> 登録したサブスクリプション情報
     *          - 201 Created：登録が成功した場合
     */
    @PostMapping
    public ResponseEntity<Subscription> addSubscription(@Valid @RequestBody Subscription subscription) {
        String email = SecurityUtils.getCurrentUserEmail();
        UUID userId = userService.getUserIdByEmail(email);

        // 不正な値をクリア
        subscription.setId(null);  // 新規作成なのでIDはnull
        subscription.setUserId(userId);  // ユーザーIDを設定
        subscription.setCreatedAt(null);  // 自動設定されるのでnull
        subscription.setUpdatedAt(null);  // 自動設定されるのでnull

        LocalDate nextPaymentDate = calculateNextPaymentDate(
                subscription.getPaymentDate(),
                subscription.getBillingCycle()
        );
        subscription.setNextPaymentDate(nextPaymentDate);

        return ResponseEntity.ok(subscriptionService.addSubscription(subscription));
    }

    /**
     * 指定されたIDのサブスクリプション情報を更新するAPIエンドポイント
     *
     * @param subscriptionId 更新対象のサブスクリプションID
     * @param subscription 更新後のサブスクリプション情報
     * @return ResponseEntity<Subscription> 更新後のサブスクリプション情報
     *          - 200 OK：更新が成功した場合
     *          - 404 Not Found：指定されたIDが存在しなかった場合
     * @throws IllegalArgumentException idまたはsubscriptionがnullの場合
     */
    @PutMapping("/{subscriptionId}")
    public ResponseEntity<Subscription> updateSubscriptionById(
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody Subscription subscription) {
        return ResponseEntity.ok(subscriptionService.updateSubscriptionById(subscriptionId, subscription));
    }

    /**
     * 指定されたIDのサブスクリプション情報を削除するAPIエンドポイント
     *
     * @param subscriptionId 削除対象のサブスクリプションID
     * @return ResponseEntity<Void>
     *         - 204 No Content: 削除が成功した場合
     *         - 404 Not Found: 指定されたIDのサブスクリプションが存在しない場合
     */
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscriptionById(@PathVariable UUID subscriptionId) {
        subscriptionService.deleteSubscriptionById(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ログインユーザーIDに紐づくサブスクリプション情報を取得するAPIエンドポイント
     *
     * @return ResponseEntity<List<Subscription>> ユーザーのサブスクリプション情報のリスト
     *         - 200 OK: 正常に取得できた場合
     *         - 404 Not Found: 指定されたユーザーIDのサブスクリプションが存在しない場合
     */
    @GetMapping("/user")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionByUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        UUID userId = userService.getUserIdByEmail(email);
        return  ResponseEntity.ok(subscriptionService.getSubscriptionByUserId(userId));
    }


    private LocalDate calculateNextPaymentDate(int paymentDate, String billingCycle) {
        LocalDate today = LocalDate.now();
        LocalDate nextPayment;

        // 支払日を基に次回支払日を計算
        if (paymentDate < today.getDayOfMonth()) {
            // 今月の支払日を過ぎている場合は来月
            nextPayment = today.plusMonths(1).withDayOfMonth(paymentDate);
        } else {
            // まだ今月の支払日が来ていない場合は今月
            nextPayment = today.withDayOfMonth(paymentDate);
        }

        // 年額の場合は1年後に設定
        if ("yearly".equals(billingCycle)) {
            nextPayment = nextPayment.plusYears(1);
        }

        return nextPayment;
    }
}