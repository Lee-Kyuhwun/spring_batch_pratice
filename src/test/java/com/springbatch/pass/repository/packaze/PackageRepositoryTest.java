package com.springbatch.pass.repository.packaze;

import com.sun.xml.bind.v2.runtime.reflect.Lister;
import org.HdrHistogram.packedarray.PackedArrayRecorder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test") // application-test.yml 파일을 사용하기 위해
class PackageRepositoryTest {

    @Autowired
    private PackageRepository packageRepository;


    @Test
    public void test_save(){
        // Given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("test");
        packageEntity.setPeriod(84);

        // When
        packageRepository.save(packageEntity);
        // Then
        assertNotNull(packageEntity.getPackageSeq());
    }


    @Test
    public void test_findByCreatedAtAfter(){
        // Given

        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1); // 현재 시간에서 1분을 뺀 시간

        PackageEntity packageEntity0 = new PackageEntity();
        packageEntity0.setPackageName("학생 전용 3개월");
        packageEntity0.setPeriod(90);


        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 6개월");
        packageEntity1.setPeriod(180);


        packageRepository.save(packageEntity0);
        packageRepository.save(packageEntity1);

        // createdAt이 dateTime 이후인 것만 조회
        // 그래서 1분전으로 설정했다.
        // When
        final List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0,1, Sort.by( "packageSeq").descending()));



        // Then
        assertEquals(1, packageEntities.size());
        assertEquals(packageEntity1.getPackageSeq(), packageEntities.get(0).getPackageSeq());
    }

    @Test
    public void test_updateCountAndPeriod(){
        /// Given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);
        // When
        int updateCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 120);
        final PackageEntity updatePackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        // Then
        assertEquals(30, updatePackageEntity.getCount());
        assertEquals(120, updatePackageEntity.getPeriod());
        assertEquals(1, updateCount);

    }

    @Test
    public void test_delete() {
        // Given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("제거할 이용권");
        packageEntity.setCount(1);
        PackageEntity newPackageEntity = packageRepository.save(packageEntity); // 저장
        // When
        packageRepository.deleteById(newPackageEntity.getPackageSeq()); // 삭제
        // Then
        assertTrue(packageRepository.findById(newPackageEntity.getPackageSeq()).isEmpty());
    }
}