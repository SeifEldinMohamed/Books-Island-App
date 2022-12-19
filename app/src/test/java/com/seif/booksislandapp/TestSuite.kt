package com.seif.booksislandapp

import com.seif.booksislandapp.data.repository.AuthRepositoryImpTest
import com.seif.booksislandapp.domain.usecase.usecase.LoginUseCaseTest
import com.seif.booksislandapp.domain.usecase.usecase.RegisterUseCaseTest
import com.seif.booksislandapp.utils.ResourceProviderTest
import com.seif.booksislandapp.utils.SharedPrefsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AuthRepositoryImpTest::class,
    RegisterUseCaseTest::class,
    LoginUseCaseTest::class,
    ResourceProviderTest::class,
    SharedPrefsTest::class
)
class TestSuite