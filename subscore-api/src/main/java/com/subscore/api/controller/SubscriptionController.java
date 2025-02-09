package com.subscore.api.controller;

import com.subscore.api.model.Subscription;
import com.subscore.api.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
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
     * <p>リクエストボディで受け取ったサブスクリプション情報を登録します。
     * 登録成功時は、作成されたリソースのURLをLocationヘッダーに含めて返却します。
     *
     * @param subscription 登録するサブスクリプション情報
     * @param uriBuilder URIビルダー
     * @return ResponseEntity<Subscription> 登録したサブスクリプション情報
     *          - 201 Created：登録が成功した場合
     * @throws IllegalArgumentException subscriptionがnullの場合
     */
    @PostMapping
    public ResponseEntity<Subscription> addSubscription(
            @Valid @RequestBody Subscription subscription,
            UriComponentsBuilder uriBuilder) {

        Subscription savedSubscription = subscriptionService.addSubscription(subscription);
        URI location = uriBuilder.path("/api/subscriptions/{id}")
                .buildAndExpand(savedSubscription.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(savedSubscription);
    }

    /**
     * 指定されたIDのサブスクリプション情報を更新するAPIエンドポイント
     *
     * @param id 更新対象のサブスクリプションID
     * @param subscription 更新後のサブスクリプション情報
     * @return ResponseEntity<Subscription> 更新後のサブスクリプション情報
     *          - 200 OK：更新が成功した場合
     *          - 404 Not Found：指定されたIDが存在しなかった場合
     * @throws IllegalArgumentException idまたはsubscriptionがnullの場合
     */
    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscriptionById(
            @PathVariable UUID id,
            @Valid @RequestBody Subscription subscription) {

        return ResponseEntity.ok(subscriptionService.updateSubscriptionById(id, subscription));
    }

    /**
     * 指定されたIDのサブスクリプション情報を削除するAPIエンドポイント
     *
     * @param id 削除対象のサブスクリプションID
     * @return ResponseEntity<Void>
     *         - 204 No Content: 削除が成功した場合
     *         - 404 Not Found: 指定されたIDのサブスクリプションが存在しない場合
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteSubscriptionById(@PathVariable UUID id) {
        subscriptionService.deleteSubscriptionById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定されたユーザーIDに紐づくサブスクリプション情報を取得するAPIエンドポイント
     *
     * @param userId 取得対象のユーザーID
     * @return ResponseEntity<List<Subscription>> ユーザーのサブスクリプション情報のリスト
     *         - 200 OK: 正常に取得できた場合
     *         - 404 Not Found: 指定されたユーザーIDのサブスクリプションが存在しない場合
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Subscription>> getSubscriptionByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByUserId(userId));
    }
}