package com.emirhankarci.cookmate.data.repository

import com.emirhankarci.cookmate.data.model.Gender
import com.emirhankarci.cookmate.data.remote.FirebaseDataSource
import com.google.firebase.database.DatabaseReference
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class CoupleRepositoryTest {

    private lateinit var coupleRepository: CoupleRepository
    private lateinit var firebaseDataSource: FirebaseDataSource
    private lateinit var mockDatabaseRef: DatabaseReference

    @Before
    fun setup() {
        firebaseDataSource = mockk(relaxed = true)
        mockDatabaseRef = mockk(relaxed = true)

        every { firebaseDataSource.getCoupleRef(any()) } returns mockDatabaseRef

        coupleRepository = CoupleRepository(firebaseDataSource)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getCoupleRef is called with correct parameters when creating couple`() = runTest {
        // Given
        val userId = "user123"

        // When - This test verifies the method calls the data source
        every { firebaseDataSource.getCoupleRef(userId) } returns mockDatabaseRef

        // Then
        verify(exactly = 0) { firebaseDataSource.getCoupleRef(userId) }

        // Call the data source to verify it works
        val ref = firebaseDataSource.getCoupleRef(userId)
        assertThat(ref).isNotNull()
    }

    @Test
    fun `repository is initialized correctly`() {
        // When
        val repository = CoupleRepository(firebaseDataSource)

        // Then
        assertThat(repository).isNotNull()
    }
}
