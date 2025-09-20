package onl.tesseract.srp.repository.hibernate.shop

import onl.tesseract.srp.domain.shop.Shop
import onl.tesseract.srp.repository.generic.ShopRepository
import onl.tesseract.srp.repository.hibernate.shop.entity.ShopEntity
import onl.tesseract.srp.repository.hibernate.shop.entity.toEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShopJpaRepository : JpaRepository<ShopEntity, Long>

@Repository
class ShopRepositoryImpl(private val jpaRepository: ShopJpaRepository) : ShopRepository {
    
    override fun save(entity: Shop): Shop {
        return jpaRepository.save(entity.toEntity()).toDomain()
    }

    override fun getById(id: Long): Shop? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }
    
    override fun idOf(entity: Shop): Long {
        // This is a simplified implementation
        return 0L // TODO: Implement proper ID extraction
    }
}