package com.rustem.eduthesis.infrastructure.security;

import com.rustem.eduthesis.infrastructure.repository.CourseRepository;
import com.rustem.eduthesis.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MyExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final CourseRepository courseRepo;
    private final UserRepository userRepo;

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> auth, MethodInvocation invocation) {
        StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(auth, invocation);
        MethodSecurityExpressionOperations delegate = (MethodSecurityExpressionOperations) context.getRootObject().getValue();
        MySecurityExpressionRoot root = new MySecurityExpressionRoot(delegate, userRepo, courseRepo);
        context.setRootObject(root);
        return context;
    }
}
