package kr.hhplus.be.server.infra.coupon.repository;

import kr.hhplus.be.server.domain.coupon.entity.CouponAssignment;
import kr.hhplus.be.server.domain.coupon.repository.CouponAssignmentRepository;
import kr.hhplus.be.server.infra.coupon.jpaRepository.CouponAssignmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponAssignmentRepositoryImpl implements CouponAssignmentRepository {

    private final CouponAssignmentJpaRepository jpaRepository;


    @Override
    public int countByCouponId(long id) {
        return jpaRepository.countByCouponId(id);
    }

    @Override
    public CouponAssignment save(CouponAssignment couponAssignment) {
        return jpaRepository.save(couponAssignment);
    }

    @Override
    public boolean existsByCouponIdAndUserId(long id, String userId) {
        return jpaRepository.existsByCouponIdAndUserId(id, userId);
    }

    @Override
    public Optional<CouponAssignment> findById(Long assignmentId) {
        return jpaRepository.findById(assignmentId);
    }

    @Override
    public Page<CouponAssignment> findByUserId(String userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<CouponAssignment> findByCouponId(long id) {
        return jpaRepository.findByCouponId(id);
    }

}
