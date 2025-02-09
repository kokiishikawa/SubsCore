package com.subscore.api.service;

import com.subscore.api.model.Subscription;
import com.subscore.api.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * サブスクリプション情報を管理するサービスクラス
 *
 * <p>このサービスは、サブスクリプション情報のビジネスロジックとデータアクセスを
 * 提供します。主にリポジトリとコントローラーの中間層として機能します。
 *
 * <p>主な機能：
 * - サブスクリプション情報の取得（全件・ユーザーID指定）
 * - サブスクリプションの登録
 * - サブスクリプション情報の更新
 * - サブスクリプションの削除
 *
 * @version 1.0.0
 * @see Subscription
 * @see SubscriptionRepository
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * 全てのサブスクリプション情報をデータベースから取得します
     *
     * @return List<Subscription> 全サブスクリプション情報のリスト
     *         （データが存在しない場合は空のリストを返却）
     */
    public List<Subscription> getSubscriptionAll() {
        return subscriptionRepository.findAll();
    }

    /**
     * 新規サブスクリプション情報を登録します
     *
     * @param subscription 登録するサブスクリプション情報
     * @return 登録されたサブスクリプション情報
     * @throws IllegalArgumentException subscriptionがnullの場合
     */
    public Subscription addSubscription(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("データがありませんでした。");
        }
        return subscriptionRepository.save(subscription);
    }

    /**
     * 指定されたIDのサブスクリプション情報を更新します
     *
     * @param id 更新対象のサブスクリプションID
     * @param subscription 更新する情報
     * @return 更新後のサブスクリプション情報
     * @throws RuntimeException 指定されたIDのサブスクリプションが存在しない場合
     * @throws IllegalArgumentException idまたはsubscriptionがnullの場合
     */
    public Subscription updateSubscriptionById(UUID id, Subscription subscription) {
        if (id == null || subscription == null) {
            throw new IllegalArgumentException("指定されたIDのデータが見つかりませんでした。");
        }

        return subscriptionRepository.findById(id)
                .map(existingSubscription -> {
                    updateSubscriptionFields(existingSubscription, subscription);
                    return subscriptionRepository.save(existingSubscription);
                })
                .orElseThrow(() -> new RuntimeException("IDまたはデータがありませんでした。"));
    }

    /**
     * 指定されたIDのサブスクリプション情報を削除します
     *
     * @param id 削除対象のサブスクリプションID
     * @throws RuntimeException 指定されたIDのサブスクリプションが存在しない場合
     * @throws IllegalArgumentException idがnullの場合
     */
    public void deleteSubscriptionById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("指定されたIDのデータが見つかりませんでした。");
        }

        if (!subscriptionRepository.existsById(id)) {
            throw new RuntimeException("ID見つかりません。");
        }
        subscriptionRepository.deleteById(id);
    }

    /**
     * 指定されたユーザーIDに紐づくサブスクリプション情報を取得します
     *
     * @param userId 取得対象のユーザーID
     * @return List<Subscription> ユーザーのサブスクリプション情報のリスト
     * @throws RuntimeException 指定されたユーザーIDのサブスクリプションが存在しない場合
     * @throws IllegalArgumentException userIdがnullの場合
     */
    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("指定されたユーザーIDのデータが見つかりませんでした。");
        }

        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            throw new RuntimeException("ユーザーIDが見つかりません。");
        }
        return subscriptions;
    }

    /**
     * サブスクリプション情報の更新処理を行うプライベートメソッド
     *
     * @param existingSubscription 更新対象の既存サブスクリプション
     * @param newSubscription 新しいサブスクリプション情報
     */
    private void updateSubscriptionFields(Subscription existingSubscription, Subscription newSubscription) {
        existingSubscription.setName(newSubscription.getName());
        existingSubscription.setPrice(newSubscription.getPrice());
        existingSubscription.setCategoryId(newSubscription.getCategoryId());
        existingSubscription.setBillingCycle(newSubscription.getBillingCycle());
        existingSubscription.setPaymentDate(newSubscription.getPaymentDate());
        existingSubscription.setNextPaymentDate(newSubscription.getNextPaymentDate());
        existingSubscription.setStatus(newSubscription.getStatus());
        existingSubscription.setNotificationEnabled(newSubscription.getNotificationEnabled());
    }
}