package ru.aristar.jnuget.query;

import java.util.Collection;
import java.util.HashSet;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.sources.PackageSource;

/**
 * Логическое умножение
 *
 * @author sviridov
 */
public class AndExpression implements Expression {

    /**
     * Первый множитель
     */
    private Expression firstExpression;
    /**
     * Второй множитель
     */
    private Expression secondExpression;

    /**
     * Конструктор по умолчанию
     */
    public AndExpression() {
    }

    /**
     * @param firstExpression первый операнд
     * @param secondExpression второй операнд
     */
    public AndExpression(Expression firstExpression, Expression secondExpression) {
        this.firstExpression = firstExpression;
        this.secondExpression = secondExpression;
    }

    @Override
    public Collection<? extends Nupkg> execute(PackageSource<? extends Nupkg> packageSource) {
        if (!getFirstExpression().hasFilterPriority() && getSecondExpression().hasFilterPriority()) {
            return getSecondExpression().filter(getFirstExpression().execute(packageSource));
        } else if (getFirstExpression().hasFilterPriority() && !getSecondExpression().hasFilterPriority()) {
            return getFirstExpression().filter(getSecondExpression().execute(packageSource));
        } else {
            HashSet<Nupkg> result = new HashSet<>();
            Collection<? extends Nupkg> firstExpressionResult = getFirstExpression().execute(packageSource);
            result.addAll(firstExpressionResult);
            Collection<? extends Nupkg> secondExpressionResult = getSecondExpression().execute(packageSource);
            result.retainAll(secondExpressionResult);
            return result;
        }
    }

    /**
     * @return Первый множитель
     */
    public Expression getFirstExpression() {
        return firstExpression;
    }

    /**
     * @param firstExpression Первый множитель
     */
    public void setFirstExpression(Expression firstExpression) {
        this.firstExpression = firstExpression;
    }

    /**
     * @return Второй множитель
     */
    public Expression getSecondExpression() {
        return secondExpression;
    }

    /**
     * @param secondExpression Второй множитель
     */
    public void setSecondExpression(Expression secondExpression) {
        this.secondExpression = secondExpression;
    }

    @Override
    public String toString() {
        return firstExpression + " and " + secondExpression;
    }

    @Override
    public Collection<? extends Nupkg> filter(Collection<? extends Nupkg> packages) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasFilterPriority() {
        return getFirstExpression().hasFilterPriority() && getFirstExpression().hasFilterPriority();
    }
}
