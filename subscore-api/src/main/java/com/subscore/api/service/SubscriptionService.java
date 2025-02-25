package com.subscore.api.service;

import com.subscore.api.dto.CategoryDTO;
import com.subscore.api.dto.SubscriptionDTO;
import com.subscore.api.model.Category;
import com.subscore.api.model.Subscription;
import com.subscore.api.repository.CategoryRepository;
import com.subscore.api.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final CategoryRepository categoryRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, CategoryRepository categoryRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.categoryRepository = categoryRepository;
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
     * @param subscriptionId 更新対象のサブスクリプションID
     * @param subscription 更新する情報
     * @return 更新後のサブスクリプション情報
     * @throws RuntimeException 指定されたIDのサブスクリプションが存在しない場合
     * @throws IllegalArgumentException idまたはsubscriptionがnullの場合
     */
    public Subscription updateSubscriptionById(UUID subscriptionId, Subscription subscription) {
        if (subscriptionId == null || subscription == null) {
            throw new IllegalArgumentException("指定されたIDのデータが見つかりませんでした。");
        }

        return subscriptionRepository.findById(subscriptionId)
                .map(existingSubscription -> {
                    updateSubscriptionFields(existingSubscription, subscription);
                    return subscriptionRepository.save(existingSubscription);
                })
                .orElseThrow(() -> new RuntimeException("IDまたはデータがありませんでした。"));
    }

    /**
     * 指定されたIDのサブスクリプション情報を削除します
     *
     * @param subscriptionId 削除対象のサブスクリプションID
     * @throws RuntimeException 指定されたIDのサブスクリプションが存在しない場合
     * @throws IllegalArgumentException idがnullの場合
     */
    public void deleteSubscriptionById(UUID subscriptionId) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("指定されたIDのデータが見つかりませんでした。");
        }

        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new RuntimeException("ID見つかりません。");
        }
        subscriptionRepository.deleteById(subscriptionId);
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
    public List<SubscriptionDTO> getSubscriptionByUserId(UUID userId) {
        // サブスクリプションを取得
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);

        // カテゴリーIDのリストを作成
        List<UUID> categoryIds = subscriptions.stream()
                .map(Subscription::getCategoryId)
                .collect(Collectors.toList());

        // カテゴリー情報を取得
        List<Category> categories = categoryRepository.findByIdIn(categoryIds);

        // カテゴリーをMapに変換して高速なルックアップを可能に
        Map<UUID, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        // DTOに変換
        return subscriptions.stream()
                .map(subscription -> {
                    Category category = categoryMap.get(subscription.getCategoryId());
                    return new SubscriptionDTO(
                            subscription.getId(),
                            subscription.getUserId(),
                            new CategoryDTO(
                                    category.getId(),
                                    category.getName()
                            ),
                            subscription.getName(),
                            subscription.getPrice(),
                            subscription.getBillingCycle(),
                            subscription.getPaymentDate(),
                            subscription.getNextPaymentDate(),
                            subscription.getStatus(),
                            subscription.getNotificationEnabled(),
                            subscription.getCreatedAt(),
                            subscription.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
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