# 아이템 16. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라
```java
class Point {
    public double x;
    public double y;
}
```
이런 클래스는 데이터 필드에 직접 접근할 수 있으니 캡슐화의 이점을 제공하지 못한다([아이템 15](item15.md)).
API를 수정하지 않고는 내부 표현을 바꿀 수 없고, 불변식을 보장할 수 없으며, 외부에서 필드에 접근할 때 부수 작업을 수행할 수도 없다.

때문에 필드를 모두 private로 바꾸고 public 접근자(getter)를 추가하는 것이 바람직하다.
```java
class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
```
패키지 바깥에서 접근할 수 있는 클래스라면 접근자를 제공함으로써 클래스 내부 표현 방식을 언제든 바꿀 수 있는 유연성을 얻을 수 있다.
public 클래스가 필드를 공개하면 이를 사용하는 클라이언트가 생겨날 것이므로 내부 표현 방식을 마음대로 바꿀 수 없게 된다.

package-private 클래스 혹은 private 중첩 클래스라면 데이터 필드를 노출해도 문제가 없다.
그 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 되고 접근자 방식보다 더 깔끔하다.
클라이언트 코드가 이 클래스 내부 표현에 묶이기는 하나, 클라이언트도 어차피 이 클래스를 포함하는 패키지 안에서 동작하는 코드이기 때문에 상관없다.
private 중첩 클래스의 경우라면 수정 범위가 더 좁아져서 이 클래스를 포함하는 외부 클래스까지로 제한된다.

자바 플랫폼 라이브러리에도 public 클래스의 필드를 직접 노출하지 말라는 규칙을 어긴 사례가 존재한다.
java.awt.package 패키지의 Point와 Dimension 클래스가 그 예시이다.
```java
/*
 * Copyright (c) 1995, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.awt;

import java.awt.geom.Dimension2D;
import java.beans.Transient;
import java.io.Serial;

/**
 * The {@code Dimension} class encapsulates the width and
 * height of a component (in integer precision) in a single object.
 * The class is
 * associated with certain properties of components. Several methods
 * defined by the {@code Component} class and the
 * {@code LayoutManager} interface return a
 * {@code Dimension} object.
 * <p>
 * Normally the values of {@code width}
 * and {@code height} are non-negative integers.
 * The constructors that allow you to create a dimension do
 * not prevent you from setting a negative value for these properties.
 * If the value of {@code width} or {@code height} is
 * negative, the behavior of some methods defined by other objects is
 * undefined.
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @see         java.awt.Component
 * @see         java.awt.LayoutManager
 * @since       1.0
 */
public class Dimension extends Dimension2D implements java.io.Serializable {

    /**
     * The width dimension; negative values can be used.
     *
     * @serial
     * @see #getSize
     * @see #setSize
     * @since 1.0
     */
    public int width;

    /**
     * The height dimension; negative values can be used.
     *
     * @serial
     * @see #getSize
     * @see #setSize
     * @since 1.0
     */
    public int height;

    /**
     * Use serialVersionUID from JDK 1.1 for interoperability.
     */
     @Serial
     private static final long serialVersionUID = 4723952579491349524L;

    /**
     * Initialize JNI field and method IDs
     */
    private static native void initIDs();

    static {
        /* ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * Creates an instance of {@code Dimension} with a width
     * of zero and a height of zero.
     */
    public Dimension() {
        this(0, 0);
    }

    /**
     * Creates an instance of {@code Dimension} whose width
     * and height are the same as for the specified dimension.
     *
     * @param    d   the specified dimension for the
     *               {@code width} and
     *               {@code height} values
     */
    public Dimension(Dimension d) {
        this(d.width, d.height);
    }

    /**
     * Constructs a {@code Dimension} and initializes
     * it to the specified width and specified height.
     *
     * @param width the specified width
     * @param height the specified height
     */
    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the size of this {@code Dimension} object to
     * the specified width and height in double precision.
     * Note that if {@code width} or {@code height}
     * are larger than {@code Integer.MAX_VALUE}, they will
     * be reset to {@code Integer.MAX_VALUE}.
     *
     * @param width  the new width for the {@code Dimension} object
     * @param height the new height for the {@code Dimension} object
     * @since 1.2
     */
    public void setSize(double width, double height) {
        this.width = (int) Math.ceil(width);
        this.height = (int) Math.ceil(height);
    }

    /**
     * Gets the size of this {@code Dimension} object.
     * This method is included for completeness, to parallel the
     * {@code getSize} method defined by {@code Component}.
     *
     * @return   the size of this dimension, a new instance of
     *           {@code Dimension} with the same width and height
     * @see      java.awt.Dimension#setSize
     * @see      java.awt.Component#getSize
     * @since    1.1
     */
    @Transient
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * Sets the size of this {@code Dimension} object to the specified size.
     * This method is included for completeness, to parallel the
     * {@code setSize} method defined by {@code Component}.
     * @param    d  the new size for this {@code Dimension} object
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    1.1
     */
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * Sets the size of this {@code Dimension} object
     * to the specified width and height.
     * This method is included for completeness, to parallel the
     * {@code setSize} method defined by {@code Component}.
     *
     * @param    width   the new width for this {@code Dimension} object
     * @param    height  the new height for this {@code Dimension} object
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    1.1
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Checks whether two dimension objects have equal values.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Dimension) {
            Dimension d = (Dimension)obj;
            return (width == d.width) && (height == d.height);
        }
        return false;
    }

    /**
     * Returns the hash code for this {@code Dimension}.
     *
     * @return    a hash code for this {@code Dimension}
     */
    public int hashCode() {
        int sum = width + height;
        return sum * (sum + 1)/2 + width;
    }

    /**
     * Returns a string representation of the values of this
     * {@code Dimension} object's {@code height} and
     * {@code width} fields. This method is intended to be used only
     * for debugging purposes, and the content and format of the returned
     * string may vary between implementations. The returned string may be
     * empty but may not be {@code null}.
     *
     * @return  a string representation of this {@code Dimension}
     *          object
     */
    public String toString() {
        return getClass().getName() + "[width=" + width + ",height=" + height + "]";
    }
}

```
[아이템 67](item67.md)에서도 설명하듯, 내부를 노출한 Dimension 클래스의 심각한 성능 문제는 해결되지 못하고 있다.

public 클래스가 불변(final)이라면 직접 노출할 때의 단점이 조금은 줄어들지만, 단점이 여전히 존재한다.
API를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을 수행할 수 없다.
단, 불변식은 보장할 수 있게 된다.
```java
public final class Time {
    private static final int HOURS_PER_DAY    = 24;
    private static final int MINUTES_PER_HOUR = 60;

    public final int hour;
    public final int minute;

    public Time(int hour, int minute) {
        if (hour < 0 || hour >= HOURS_PER_DAY)
            throw new IllegalArgumentException("시간: " + hour);
        if (minute < 0 || minute >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("분: " + minute);
        this.hour = hour;
        this.minute = minute;
    }
    // 나머지 코드 생략
}

```
